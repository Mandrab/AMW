package controller.admin.agent

import controller.admin.agent.Proxy.Proxy
import jade.core.Agent as JadeAgent

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class Agent: JadeAgent() {
    private val client: String by lazy { arguments[1] as String }
    private val clientMail: String by lazy { arguments[2] as String }

    override fun setup() {
        super.setup()
        (arguments[0] as Proxy).setAgent(this)
    }

    fun shutdown() = super.takeDown()

    fun addCommand() { TODO() }

    fun addVersion() { TODO() }

    fun executeCommand() { TODO() }

    fun executeScript() { TODO() }
}