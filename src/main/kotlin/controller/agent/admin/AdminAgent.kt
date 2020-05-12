package controller.agent.admin

import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jason.asSyntax.Structure
import controller.agent.client.ClientAgent
import java.util.*
import common.type.Command
import common.translation.LiteralParser.split
import common.translation.ServiceType.*

/**
 * Agent for admin-side application.
 * It allows to communicate to obtain commands/items information,
 * additionally, allow to ask scripts/commands run
 *
 * @author Paolo Baldini
 */
class AdminAgent: ClientAgent() {
    private val proxy: AdminProxy by lazy { arguments[0] as AdminProxy }
    private var lastUpdate: Long = 0

    override fun setup() {
        super.setup()
        addBehaviour(updateCommands())                              // add update commands behaviour
    }

    /**
     * Periodically updates commands list
     */
    private fun updateCommands() = object : CyclicBehaviour() {
        override fun action() {
            if (Date().time - lastUpdate > UPDATE_TIME) {           // update repository (commands) info periodically

                // setup the message and send it
                val info = Structure("info")
                info.addTerm(Structure("commands"))

                MessageSender(MANAGEMENT_COMMANDS.service, INFO_COMMANDS.service, ACLMessage.CFP, info)
                    .require(agent).thenAccept { message ->
                        proxy.onNext(split(message!!.content).map { Command.parse(it) })
                    }
                lastUpdate = Date().time
            }
            block(UPDATE_TIME.toLong())                             // wait specified time before update again
        }
    }

    companion object {
        private const val UPDATE_TIME = 1000                        // update period time
    }
}