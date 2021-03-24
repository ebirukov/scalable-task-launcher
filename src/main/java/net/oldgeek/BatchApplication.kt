package net.oldgeek

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.integration.annotation.IntegrationComponentScan
import kotlin.jvm.JvmStatic
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.integration.annotation.MessagingGateway
import net.oldgeek.ProducerChannels
import net.oldgeek.JobDefinition
import org.springframework.web.bind.annotation.RestController
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.beans.factory.annotation.Autowired
import net.oldgeek.GreetingGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.integration.launch.JobLaunchRequest
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.ResponseEntity
import org.springframework.integration.annotation.Gateway

@SpringBootApplication
@IntegrationComponentScan
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}

data class JobDefinition(val jobName: String, val job: Job?, val time: Long)

@MessagingGateway
@Profile("!worker")
interface GreetingGateway {
    @Gateway(requestChannel = ProducerChannels.DIRECT)
    fun directGreet(msg: JobDefinition)
}

@RestController
@EnableBinding(ProducerChannels::class)
@Profile("!worker")
class GreetingProducer(private val gateway: GreetingGateway) {

    @RequestMapping(method = [RequestMethod.GET], value = ["/hi/{name}"])
    fun hi(@PathVariable name: String): ResponseEntity<*> {
        val message = "Hello, $name!"
        gateway.directGreet(JobDefinition("Direct: $message", null, System.nanoTime()))
        return ResponseEntity.ok(message)
    }
}


@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
            .registerModules(KotlinModule())
            .addMixIn(StepExecution::class.java, StepExecutionMixin::class.java)
            .addMixIn(JobExecution::class.java, JobExecutionMixin::class.java)
            //.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            //.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    }

    abstract class JobExecutionMixin(@JsonManagedReference val stepExecutions: Collection<StepExecution>)

    abstract class StepExecutionMixin(@JsonBackReference val jobExecution: JobExecution)
}

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)