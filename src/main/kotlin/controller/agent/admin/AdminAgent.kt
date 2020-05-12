package controller.agent.admin

import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jason.asSyntax.Structure
import java.util.*
import common.type.Command
import common.translation.LiteralParser.split
import common.translation.Service.*
import controller.agent.abstracts.ItemUpdater

/**
 * Agent for admin-side application.
 * It allows to communicate to obtain commands/items information,
 * additionally, allow to ask scripts/commands run
 *
 * @author Paolo Baldini
 */
class AdminAgent: ItemUpdater() {
    private var lastUpdate: Long = 0

    override val proxy: AdminProxy by lazy { arguments[0] as AdminProxy }
    override val updateTime: Long = 1000                            // update period time

    override fun setup() {
        super.setup()
        proxy.setAgent(this)
        addBehaviour(updateCommands())                              // add update-commands behaviour
    }

    /**
     * Periodically updates commands list
     */
    private fun updateCommands() = object : CyclicBehaviour() {
        override fun action() {                                     // update repository (commands) info periodically
            // setup the message and send it
            val info = Structure("info")
            info.addTerm(Structure("commands"))

            MessageSender(MANAGEMENT_COMMANDS.service, INFO_COMMANDS.service, ACLMessage.CFP, info).require(agent)
                .thenAccept { message -> proxy.dispatchCommands(split(message!!.content).map { Command.parse(it) }) }
            lastUpdate = Date().time

            while (Date().time - lastUpdate < updateTime)
                block(updateTime)                                   // wait specified time before update again
        }
    }

    fun shutdown() = super.takeDown()                               // TODO make behaviour to close things
}