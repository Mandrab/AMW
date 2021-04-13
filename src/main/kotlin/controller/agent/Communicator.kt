package controller.agent

import controller.agent.Agents.cyclicBehaviour
import controller.agent.Agents.receiveContent
import controller.agent.Agents.receiveId
import jade.core.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.FAILURE
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
        // gather all the messages that obtained a valid response
        val responses = waitingConfirm.map { Pair(it, receiveId(it.message.replyWith)) }.filter { it.second != null }
            .filter { it.second?.performative != FAILURE || !it.first.retryOnFailure }

        // drop all the confirmed messages
        waitingConfirm = waitingConfirm.filterNot { responses.any { (message, _) -> message == it } }

        // complete future for all the received messages
        responses.forEach { it.first.response.complete(it.second) }

        // drop "received" messages that are not expected TODO: maybe fare che droppo tutto ciò che non è in waitConfirm
        generateSequence { receiveContent("received") }

        agent.block()
    }

    /**
     * Send a message expecting a response
     */
    fun sendMessage(message: ACLMessage) = sendMessage(message) { it }

    /**
     * Send a message expecting a response.
     * When the response arrives, extract needed information from it
     */
    fun <T> sendMessage(message: ACLMessage, retryOnFailure: Boolean = true, mapTo: (ACLMessage) -> T): Future<T> =
        CompletableFuture<T>().completeAsync {
                ResponseMessage(
                    message.apply { replyWith = replyWith ?: let { Date().time.toString() } },
                    retryOnFailure
                ).apply {
                    waitingConfirm += this
                    send(message)
                }.response.get().let(mapTo)
            }

    /**
     * Periodically resend messages that need a response
     */
    private fun requestConfirmations() = cyclicBehaviour { agent ->
        waitingConfirm.filter { Date().seconds - it.lastSent.seconds > 3 }.forEach {
            it.lastSent = Date()
            send(it.message)
        }
        agent.block(updateTime)
    }

    private data class ResponseMessage(
        val message: ACLMessage,
        val retryOnFailure: Boolean
    ) {
        var lastSent: Date = Date()
        val response: CompletableFuture<ACLMessage> = CompletableFuture()
    }
}