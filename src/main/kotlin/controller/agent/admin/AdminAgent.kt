package controller.agent.admin

import common.translation.LiteralBuilder
import common.translation.LiteralParser.getValue
import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jason.asSyntax.Structure
import java.util.*
import common.type.Command
import common.translation.LiteralParser.split
import common.translation.Service.*
import controller.agent.abstracts.ItemUpdater
import jason.asSyntax.Literal
import jason.asSyntax.StringTermImpl
import java.util.concurrent.CompletableFuture

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

    fun execute(commandID: String): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()

        val execute: Literal = LiteralBuilder("execute").setValues(LiteralBuilder("command_id")
            .setValues(commandID).build()).build()
        MessageSender(EXECUTOR_COMMAND.service, EXEC_COMMAND.service, ACLMessage.CFP, execute).require(this)
            .thenAccept {
                it ?: result.complete(false)
                when (it?.performative) {
                    ACLMessage.REFUSE -> result.complete(false)
                    ACLMessage.PROPOSE -> {
                        MessageSender(it.sender, ACLMessage.ACCEPT_PROPOSAL, LiteralBuilder("execute").setValues(
                                LiteralBuilder("command_id").setValues(getValue(it.content, "command_id")!!).build()
                            ).build()
                        ).send(this)
                        result.complete(true)
                    }
                }
            }

        return result
    }

    fun execute(script: String, requirements: Set<String>): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()

        val scriptLiteral: Literal = LiteralBuilder("script").setValues(StringTermImpl(script))
            .setQueue(*requirements.map { StringTermImpl(it) }.toTypedArray()).build()
        val executeLiteral = LiteralBuilder("execute").setValues(scriptLiteral).build()

        MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP, executeLiteral).require(this)
			.thenAccept {
                it ?: result.complete(false) //TODO timeout
                when (it?.performative) {
                    ACLMessage.REFUSE -> result.complete(false)
                    ACLMessage.PROPOSE -> {
                        MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.ACCEPT_PROPOSAL,
                            LiteralBuilder("execute").setValues("script").build()).setMsgID(it.inReplyTo).send(this)
                        result.complete(true)
                    }
                    ACLMessage.FAILURE -> TODO()
                }
			}

        return result
    }

    fun shutdown(): CompletableFuture<Unit> {
        // TODO make behaviour to close things
        super.takeDown()
        return CompletableFuture.completedFuture(Unit)
    }
}