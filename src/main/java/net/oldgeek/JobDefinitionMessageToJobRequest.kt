package net.oldgeek

import net.oldgeek.JobParameterConfigurator.Companion.convertToJobParams
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.integration.annotation.Transformer
import org.springframework.stereotype.Component
import java.util.*

// See http://docs.spring.io/spring-batch/trunk/reference/html/springBatchIntegration.html#launching-batch-jobs-through-messages
@Component
class JobDefinitionMessageToJobRequest(
       private val jobTypeResolver: JobTypeResolver
    ) {

    @Transformer
    fun toRequest(jobDefinition: JobDefinition): JobLaunchRequest {
        return JobLaunchRequest(jobTypeResolver.resolveJob(jobDefinition), jobDefinition.jobType.params.convertToJobParams())
    }
}

@Component
class JobTypeResolver(private val batchConfig: BatchConfig) {

    fun resolveJob(jobDefinition: JobDefinition) = when(jobDefinition.jobType) {
        is SimpleJobType -> batchConfig.singleJob(jobDefinition.jobName, jobDefinition.jobType.taskExecutor)
        is ComplexJobType -> batchConfig.complexJob(jobDefinition.jobName)
    }

}

class JobParameterConfigurator {

    companion object {
        internal fun Map<String, Any>.convertToJobParams() =
            JobParameters( mapValues { it.value.toJobParam() } )

        private fun Any.toJobParam() =
            when (this) {
                is Long -> JobParameter(this)
                is Date -> JobParameter(this)
                is String -> JobParameter(this)
                is Double -> JobParameter(this)
                else -> throw JobParametersInvalidException("can't convert $this")
            }
    }
}