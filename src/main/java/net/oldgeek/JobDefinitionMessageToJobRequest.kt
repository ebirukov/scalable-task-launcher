package net.oldgeek

import net.oldgeek.JobDefinition
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.integration.annotation.Transformer
import org.springframework.stereotype.Component

// See http://docs.spring.io/spring-batch/trunk/reference/html/springBatchIntegration.html#launching-batch-jobs-through-messages
@Component
class JobDefinitionMessageToJobRequest(
        private val parameterConfigurator: JobParameterConfigurator,
       private val jobTypeResolver: JobTypeResolver
    ) {

    @Transformer
    fun toRequest(jobDefinition: JobDefinition): JobLaunchRequest {
        return JobLaunchRequest(jobTypeResolver.resolveJob(jobDefinition), parameterConfigurator.prepareJobParameters(jobDefinition))
    }
}

@Component
class JobTypeResolver(private val batchConfig: BatchConfig) {

    fun resolveJob(jobDefinition: JobDefinition) = when(jobDefinition.jobType) {
        JobType.SIMPLE -> batchConfig.singleJob(jobDefinition.jobName)
            JobType.COMPLEX -> batchConfig.complexJob(jobDefinition.jobName)
    }

}

@Component
class JobParameterConfigurator {

    fun prepareJobParameters(jobDefinition: JobDefinition): JobParameters {
        val jobParametersBuilder = JobParametersBuilder()
        jobParametersBuilder.addString("name", jobDefinition.jobName)
        jobParametersBuilder.addLong("dummy", jobDefinition.time)
        return jobParametersBuilder.toJobParameters()
    }
}