package controller.agent

import controller.agent.Agents.cyclicBehaviour
import controller.agent.Agents.receiveContent
import jade.core.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.FAILURE
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Provides useful methods and utilities for working with agent entities.
 * Mostly, it encapsulate a way for 'retry' message sending after a possible failure.
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
     *
     * @return a cyclic behaviour for the task
     */
    private fun checkMessages() = cyclicBehaviour { behaviour ->
        // retrieve a message from the received ones; find the relative message. Block if none is available
        val response = receive() ?: return@cyclicBehaviour behaviour.block()
        val message = waitingConfirm.find { it.message.replyWith == response.inReplyTo } ?: return@cyclicBehaviour

        // if failure response and I want to retry, ignore the message
        if (response.performative == FAILURE && message.retryOnFailure) return@cyclicBehaviour

        waitingConfirm = waitingConfirm.filterNot { it == message }         // drop the responded message

        message.finalize(response)                                          // complete future for the responded message

        // drop "received" messages that are not expected TODO: maybe fare che droppo tutto ciò che non è in waitConfirm
        generateSequence { receiveContent("received") }

        behaviour.block()                                                   // block behaviour until new messages
    }

    /**
     * Send a message expecting a response
     *
     * @return a future representing the response
     */
    fun sendMessage(message: ACLMessage) = sendMessage(message) { it }

    /**
     * Send a message expecting a response.
     * When the response arrives, extract needed information from it
     *
     * @return a future representing the response
     */
    fun <T> sendMessage(message: ACLMessage, retryOnFailure: Boolean = true, mapTo: (ACLMessage) -> T): Future<T> =
        CompletableFuture<T>().also { future ->
            message.replyWith = message.replyWith ?: Instant.now().run { toString() + nano }
            waitingConfirm += ResponseMessage(message, retryOnFailure) { future.complete(mapTo(it)) }
            send(message)
        }

    /**
     * Periodically resend messages that need (and still dont have) a response
     *
     * @return a cyclic behaviour for the task
     */
    private fun requestConfirmations() = cyclicBehaviour { agent ->
        waitingConfirm.filter { it.age() > 2 }.forEach {                    // find not responded messages
            it.lastSent = Instant.now()                                     // update send time
            send(it.message)                                                // resend them
        }
        agent.block(updateTime)                                             // block for a while
    }

    /**
     * Dataclass for messages information storing
     */
    private data class ResponseMessage(
        val message: ACLMessage,
        val retryOnFailure: Boolean,
        val finalize: (ACLMessage) -> Unit
    ) {
        var lastSent: Instant = Instant.now()

        /**
         * Gives message age from its last sent
         *
         * @return seconds from last sent
         */
        fun age() = Duration.between(lastSent, Instant.now()).seconds
    }
}
