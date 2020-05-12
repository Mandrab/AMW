package controller.agent

import jade.core.behaviours.OneShotBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import java.io.Serializable
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import jade.core.*

/**
 * A class that encapsulate useful communication features
 *
 * @author Paolo Baldini
 */
abstract class TerminalAgent: Agent() {
    private val outdatedMsgTemplate: MutableList<MessageTemplate> = mutableListOf()

    override fun setup() {
        addBehaviour(collectOutdatedResponses())
    }

    /**
     * Remove un-responded OLD messages (the ones with specified timeout)
     */
    private fun collectOutdatedResponses() = object: OneShotBehaviour() {
        override fun action() {
            if (outdatedMsgTemplate.size > 0) receive(outdatedMsgTemplate.removeAt(0)) // TODO nel caso di molti messaggi non blocca per troppo tempo l'esecuzione
            block()
        }
    }

    /**
     * An utility class to sends messages comfortably
     *
     * @author Paolo Baldini
     */
    protected inner class MessageSender {
        private val message: ACLMessage by lazy { ACLMessage() }
        private val receiversTemplates by lazy { mutableListOf<DFAgentDescription>() }
        private var timeout = RESPONSE_TIME.toLong() // -1 = disabled

        /**
         * Construct message specifying receiver AID
         */
        constructor(agent: AID, performative: Int, content: Serializable) {
            addReceivers(agent)
            setMessage(performative, content)
        }

        /**
         * Construct message specifying receivers template
         */
        constructor(template: DFAgentDescription, performative: Int, content: Serializable) {
            addReceivers(template)
            setMessage(performative, content)
        }

        /**
         * Construct message specifying service name & type
         */
        constructor(serviceName: String?, serviceType: String?, performative: Int, content: Serializable) {
            addReceiver(serviceName, serviceType)
            setMessage(performative, content)
        }

        /**
         * Sets reply-with field in message
         */
        fun setMsgID(id: String?) = apply { message.replyWith = id }

        fun setMessage(performative: Int, content: Serializable) = apply {
            message.performative = performative // create a "call for propose" message
            if (content is String) message.content = content
            else message.contentObject = content
        }

        fun addReceivers(vararg agents: AID) = apply { agents.forEach { message.addReceiver(it) } }

        fun addReceivers(vararg templates: DFAgentDescription) = apply { receiversTemplates.addAll(listOf(*templates)) }

        fun addReceiver(serviceName: String?, serviceType: String?) = apply {
            val receiverTemplate = DFAgentDescription() // create a "service provider" template
            val sd = ServiceDescription()
            sd.name = serviceName
            sd.type = serviceType
            receiverTemplate.addServices(sd)
            receiversTemplates.add(receiverTemplate)
        }

        /**
         * Sets response timeout.
         * if value is -1, then timeout is disabled
         */
        fun setTimeout(duration: Long) = apply { timeout = duration }

        fun send(sender: Agent) {
            check(!message.allReceiver.hasNext() || receiversTemplates.isEmpty())
                    { "At least a receiver must be specified" }
            Executors.newCachedThreadPool().submit {        // TODO inline?
                receiversTemplates.forEach { it ->
                    try {
                        val result = DFService.search(sender, it) // an array containing all the agents that matches the template
                        if (result.isEmpty()) return@submit
                        result.forEach { message.addReceiver(it.name) }
                    } catch (e: FIPAException) { e.printStackTrace() }
                }
                sender.send(message) // send the cfp to all ability sellers
            }
        }

        /**
         * Send a message expecting a response
         */
        fun require(sender: Agent): CompletableFuture<ACLMessage?> {
            val returnValue = CompletableFuture<ACLMessage?>()
            val msgId = LocalDateTime.now().toString() + String.format("%.10f", Math.random())
            message.replyWith = msgId
            send(sender)
            Executors.newCachedThreadPool().submit {
                val s = if (timeout != -1L)
                        blockingReceive(MessageTemplate.MatchInReplyTo(msgId), timeout)
                else blockingReceive(MessageTemplate.MatchInReplyTo(msgId))
                s ?: outdatedMsgTemplate.add(MessageTemplate.MatchInReplyTo(msgId)) // collect delayed message and cancel it
                returnValue.complete(s)
            }
            return returnValue
        }
    }

    companion object {
        private const val RESPONSE_TIME = 50000
    }
}