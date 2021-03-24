package net.oldgeek

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface ProducerChannels {
    @Input(BROADCAST)
    fun jobRequestInbound(): MessageChannel

    @Output(DIRECT)
    fun jobRequestOutbound(): MessageChannel

    companion object {
        const val DIRECT = "jobRequestOutbound"
        const val BROADCAST = "jobRequestInbound"
    }
}