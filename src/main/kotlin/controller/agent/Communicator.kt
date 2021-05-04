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

/**
 * Provides useful methods and utilities for working with agent entities
 *
 * @author Paolo Baldini
 */
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
        responses.forEach { it.first.finalize(it.second!!) }

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
        CompletableFuture<T>().also { future ->
            message.replyWith = message.replyWith ?: Date().time.toString()
            waitingConfirm += ResponseMessage(message, retryOnFailure) { future.complete(mapTo(it)) }
            send(message)
        }

    /**
     * Periodically resend messages that need a response
     */
    private fun requestConfirmations() = cyclicBehaviour { agent ->
        waitingConfirm.filter { Date().seconds - it.lastSent.seconds > 2 }.forEach {// TODO is minus correct?
            it.lastSent = Date()
            send(it.message)
        }
        agent.block(updateTime)
    }

    private data class ResponseMessage(
        val message: ACLMessage,
        val retryOnFailure: Boolean,
        val finalize: (ACLMessage) -> Unit
    ) {
        var lastSent: Date = Date()
    }
}