package controller.agent

import controller.agent.Agents.cyclicBehaviour
import controller.agent.Agents.receiveContent
import controller.agent.Agents.receiveId
import jade.core.Agent
import jade.lang.acl.ACLMessage
import java.util.Date
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

abstract class Communicator: Agent() {
    private val updateTime = 2500L
    private var waitingConfirm = emptyList<ResponseMessage>()

    override fun setup() {
        super.setup()
        addBehaviour(checkMessages())
        addBehaviour(requestConfirmations())
    }

    /**
     * Periodically check if a response messages has come
     */
    private fun checkMessages() = cyclicBehaviour { agent ->
        // gather all the messages that obtained a response
        val responses = waitingConfirm.map { Pair(it, receiveId(it.message.inReplyTo)) }.filter { it.second != null }

        // drop all the confirmed messages
        waitingConfirm = waitingConfirm.filterNot { responses.any { (message, _) -> message == it } }

        // complete future for all the received messages
        responses.forEach { it.first.response.complete(it.second) }

        // drop "received" messages that are not expected TODO: maybe fare che droppo tutto ciò che non è in waitConfirm
        generateSequence { receiveContent("received") }

        agent.block(updateTime)
    }

    /**
     * Send a message expecting a response
     */
    fun sendMessage(message: ACLMessage) = sendMessage(message) { it }

    /**
     * Send a message expecting a response.
     * When the response arrives, extract needed information from it
     */
    fun <T> sendMessage(message: ACLMessage, mapTo: (ACLMessage) -> T): Future<T> = CompletableFuture<T>()
            .completeAsync {
                ResponseMessage(message.apply { inReplyTo = inReplyTo ?: let { Date().time.toString() } }).apply {
                    waitingConfirm += this
                    send(message)
                }.response.get().let(mapTo)
            }

    /**
     * Periodically resend messages that need a response
     */
    private fun requestConfirmations() = cyclicBehaviour { agent ->
        waitingConfirm.forEach { send(it.message) }
        agent.block(updateTime)
    }

    private data class ResponseMessage(
        val message: ACLMessage,
        val response: CompletableFuture<ACLMessage> = CompletableFuture()
    )
}