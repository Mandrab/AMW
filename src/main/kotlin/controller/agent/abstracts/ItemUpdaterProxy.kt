package controller.agent.abstracts

import common.type.Item
import controller.agent.AgentProxy
import io.reactivex.rxjava3.core.Observer
import jade.core.Agent

/**
 * ItemUpdater-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this proxy.
 * In the application however, it's only extended.
 *
 * @author Paolo Baldini
 */
open class ItemUpdaterProxy: AgentProxy {
	private val itemSubscribers = mutableSetOf<Observer<Collection<Item>>>()
	private lateinit var agent: ItemUpdater

	/** {@inheritDoc} */
	override fun setAgent(agent: Agent) {
		check(agent is ItemUpdater) { "Erroneous use of AdminProxy" }
		this.agent = agent
	}

	/** {@inheritDoc} */
	override fun isAvailable(): Boolean = this::agent.isInitialized

	/**
	 * Set an observer who will be notified at items update
	 */
	fun subscribeItems(s: Observer<Collection<Item>>) = itemSubscribers.add(s)

	/**
	 * Notify observers with a new items list
	 */
	fun dispatchItems(t: Collection<Item>) { itemSubscribers.onEach { it.onNext(t) } }
}