package controller.agent.admin

import common.Request
import common.Request.EXEC_COMMAND
import common.Request.EXEC_SCRIPT
import common.Request.END
import common.type.Command
import controller.agent.abstracts.ItemUpdaterProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.functions.Consumer
import jade.core.Agent

/**
 * Admin-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this
 *
 * @author Paolo Baldini
 */
class AdminProxy: ItemUpdaterProxy(), Consumer<Pair<Request, Array<out Any>>> {
	private val commandSubscribers = mutableSetOf<Observer<in Collection<Command>>>()
	private lateinit var agent: AdminAgent

	/** {@inheritDoc} */
	override fun setAgent(agent: Agent) {
		check(agent is AdminAgent) { "Erroneous use of AdminProxy" }
		this.agent = agent
	}

	/** {@inheritDoc} */
	override fun isAvailable(): Boolean = this::agent.isInitialized

	/**
	 * Set an observer who will be notified at commands update
	 */
	fun subscribeCommands(s: Observer<in Collection<Command>>) { commandSubscribers.add(s) }

	/**
	 * Notify observers with a new commands list
	 */
	fun dispatchCommands(t: Collection<Command>) { commandSubscribers.onEach { it.onNext(t) } }

	/** {@inheritDoc} */
	override fun accept(t: Pair<Request, Array<out Any>>) {
		when (t.first) {
			EXEC_COMMAND -> {
				TODO()
				/*val execute: Literal =
                LiteralBuilder("execute").setValues(LiteralBuilder("command_id").setValues(args[0]).build())
                    .build()
	            MessageSender(EXECUTOR_COMMAND.name, EXEC_COMMAND.name, ACLMessage.CFP, execute).require(this)
	                .thenAccept(Consumer { c: ACLMessage? ->
	                    try {
	                        if (c == null) return@Consumer   // timeout
	                        if (c.performative == ACLMessage.REFUSE) {                     // refuse to propose
	                            // TODO
	                        } else if (c.performative == ACLMessage.PROPOSE) {             // accept
	                            MessageSender(
	                                c.sender, ACLMessage.ACCEPT_PROPOSAL, LiteralBuilder("execute").setValues(
	                                        LiteralBuilder("command_id")
	                                            .setValues(getValue(c.content, "command_id")!!).build()
	                                    ).build()
	                            ).send(this)
	                        }
	                    } catch (e: Exception) {
	                        e.printStackTrace()
	                    }
	                })
	            return null*/
			}
			EXEC_SCRIPT -> {
				TODO()
				/*val script = args[0]
	            val scriptLit: Literal = LiteralBuilder("script").setValues(StringTermImpl(script))
	                .setQueue(*args.map { StringTermImpl(it) }.subList(1, args.size).toTypedArray()).build()
	            val execute: Literal = LiteralBuilder("execute").setValues(scriptLit).build()
	            MessageSender(EXECUTOR_SCRIPT.name, EXEC_SCRIPT.name, ACLMessage.CFP, execute).require(this)
	                .thenAccept(Consumer { c: ACLMessage? ->
	                    if (c == null) return@Consumer   // timeout
	                    if (c.performative == ACLMessage.REFUSE) {                     // refuse to propose
	                        // TODO
	                    } else if (c.performative == ACLMessage.PROPOSE) {             // accept
	                        MessageSender(
	                            EXECUTOR_SCRIPT.name, EXEC_SCRIPT.name, ACLMessage.ACCEPT_PROPOSAL,
	                            LiteralBuilder("execute").setValues("script").build()
	                        ).setMsgID(c.inReplyTo)
	                            .send(this)
	                    }
	                })
	            return null*/
			}
			END -> agent.shutdown()
			else -> Unit
		}
	}
}