package net.oldgeek

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableBatchProcessing
class BatchConfig(
    var jobBuilderFactory: JobBuilderFactory,
    var stepBuilderFactory: StepBuilderFactory
) {

    fun sampleStep(name: String): Step {
        return stepBuilderFactory[name]
            .tasklet { _: StepContribution, chunkContext: ChunkContext ->
                val l = chunkContext.stepContext.jobParameters["dummy"] as Long
                if (l % 2 == 0L) Thread.sleep(6000)
                RepeatStatus.FINISHED
            } //.taskExecutor(new SimpleAsyncTaskExecutor("batch"))
            .build()
    }

    fun singleJob(jobName: String): Job {
        return jobBuilderFactory[jobName]
            .incrementer(RunIdIncrementer())
            .start(sampleStep("sampleStep"))
            .build()
    }

    fun complexJob(jobName: String): Job {
        return jobBuilderFactory[jobName]
            .incrementer(RunIdIncrementer())
            .start(sampleStep("step1")).next(sampleStep("step2")).next(sampleStep("step3"))
            .build()
    }
}