package controller.agent

import controller.agent.Agents.cyclicBehaviour
import controller.agent.Agents.matchContent
import controller.agent.Agents.receiveContent
import controller.agent.Agents.receiveId
import jade.core.Agent
import jade.lang.acl.ACLMessage

abstract class Communicator: Agent() {
    private var waitingConfirm = emptyList<ACLMessage>()

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
        val confirmedMessages = waitingConfirm.map { Pair(it, receiveId(it.inReplyTo)) }.filter { it.second != null }

        // drop all the confirmed messages
        waitingConfirm = waitingConfirm.filterNot { confirmedMessages.any { (message, _) -> message == it } }

        // restore all the messages that have a content different from a simple "received"
        confirmedMessages.filterNot { it.second!!.matchContent("received") }.forEach { putBack(it.second) }

        // drop "received" messages that are not expected
        generateSequence { receiveContent("received") }

        agent.block()
    }

    /**
     * Periodically resend messages that need a response
     */
    private fun requestConfirmations() = cyclicBehaviour { agent ->
        waitingConfirm.forEach { send(it) }
        agent.block()
    }

    /**
     * Send a message expecting a response
     */
    fun sendMessage(message: ACLMessage) {
        waitingConfirm = waitingConfirm + message
        send(message)
    }
}