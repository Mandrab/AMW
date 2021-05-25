package controller.agent.communication

import jade.core.Agent
import jade.core.NotFoundException
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import java.io.Serializable
import java.util.*

/**
 * Utilities for more clean message sending
 *
 * @author Paolo Baldini
 */
object Messages {

    class Message {
        private val message: ACLMessage = ACLMessage(ACLMessage.INFORM).apply {
            replyWith = Date().time.toString()
        }
        private var receivers = emptySet<DFAgentDescription>()

        var performative: Int
            get() = message.performative
            set(value) { message.performative = value }

        var replyWith: String
            get() = message.replyWith
            set(value) { message.replyWith = value }

        var payload: String
            get() = message.content
            set(value) { message.content = value }

        var payloadObject: Serializable
            get() = message.contentObject
            set(value) { message.contentObject = value }

        fun receivers(vararg receivers: Receiver) { this.receivers += receivers.map { descriptor(it) } }

        operator fun invoke(agent: Agent) = message.apply {
            receivers.flatMap { DFService.search(agent, it).asSequence() }.ifEmpty { throw NotFoundException() }
                .forEach { message.addReceiver(it.name) }
        }

        private fun descriptor(receiver: Receiver) = DFAgentDescription().apply {
            addServices(ServiceDescription().apply {
                name = receiver.supplier
                type = receiver.service
            })
        }
    }

    class Receiver {
        lateinit var supplier: String
        lateinit var service: String
    }

    fun message(init: Message.() -> Unit): Message = Message().apply(init)

    fun receiver(init: Receiver.() -> Unit): Receiver = Receiver().apply(init)
}
