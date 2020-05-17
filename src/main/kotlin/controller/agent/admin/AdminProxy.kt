package controller.agent.admin

import common.type.Command
import controller.agent.abstracts.ItemUpdaterProxy
import io.reactivex.rxjava3.core.Observer
import jade.core.Agent
import java.util.concurrent.Future

/**
 * Admin-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this
 *
 * @author Paolo Baldini
 */
class AdminProxy: ItemUpdaterProxy() {
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

	fun add(command: Command) = agent.add(command)

	fun add(commandID: String, version: Command.Version) = agent.add(commandID, version)

	fun execute(commandID: String) = agent.execute(commandID)

	fun execute(script: String, requirements: Set<String>) = agent.execute(script, requirements)

	fun stop(): Future<Unit> = agent.shutdown()
}