package net.oldgeek

import net.oldgeek.JobDefinition
import org.springframework.batch.core.Job
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.integration.annotation.Transformer

// See http://docs.spring.io/spring-batch/trunk/reference/html/springBatchIntegration.html#launching-batch-jobs-through-messages
class FileMessageToJobRequest(private var job: Job) {

    @Transformer
    fun toRequest(message: JobDefinition): JobLaunchRequest {
        val jobParametersBuilder = JobParametersBuilder()
        jobParametersBuilder.addString("name", message.jobName)
        jobParametersBuilder.addLong("dummy", message.time)
        return JobLaunchRequest(job, jobParametersBuilder.toJobParameters())
    }
}