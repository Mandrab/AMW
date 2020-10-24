package controller.agent.communication

import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import java.io.Serializable
import java.util.*

object Messages {

    class Message {

        val message: ACLMessage = ACLMessage().apply {
            performative = ACLMessage.INFORM
            replyWith = Date().toString()
        }

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

        fun receivers(init: Receiver.() -> Unit) {
            message.addReceiver(receiver(Receiver().apply(init)).name)
        }

        private fun receiver(receiver: Receiver) =
            DFAgentDescription().apply {
                addServices(ServiceDescription().apply {
                    name = receiver.name
                    type = receiver.type
                })
            }
    }

    class Receiver {
        lateinit var name: String
        lateinit var type: String
    }

    fun message(init: Message.() -> Unit): ACLMessage = Message().apply(init).message

    fun receiver(init: Receiver.() -> Unit): Receiver = Receiver().apply(init)
}