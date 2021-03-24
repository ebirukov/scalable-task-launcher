package net.oldgeek

import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.batch.core.Job
import org.springframework.batch.integration.launch.JobLaunchingGateway
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
@EnableBinding(ProducerChannels::class)
class IntegrationConfig(
    private val jobRepository: JobRepository,
    private val producerChannels: ProducerChannels,
    private val jobDefinitionMessageToJobRequest: JobDefinitionMessageToJobRequest
) {

    @Bean
    fun sampleFlow(): IntegrationFlow {
        return IntegrationFlows.from(producerChannels.jobRequestInbound())
            .transform(jobDefinitionMessageToJobRequest)
            .handle(jobLaunchingGateway())
            .handle { jobExecution: Message<*> -> println(jobExecution.payload) }
            .get()
    }


    @Bean
    fun jobLaunchingGateway(): JobLaunchingGateway {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor())
        return JobLaunchingGateway(jobLauncher)
    }
}