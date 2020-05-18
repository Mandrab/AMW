package controller.agent.client

import controller.agent.abstracts.ItemUpdaterProxy
import io.reactivex.rxjava3.core.Observer
import jade.core.Agent
import common.type.Order
import java.util.concurrent.Future

/**
 * Client-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this
 *
 * @author Paolo Baldini
 */
class ClientProxy: ItemUpdaterProxy() {
	private val orderObservers = mutableSetOf<Observer<Order>>()
	private lateinit var agent: ClientAgent

	/** {@inheritDoc} */
	override fun setAgent(agent: Agent) {
		check(agent is ClientAgent) { "Erroneous use of ClientProxy" }
		this.agent = agent
	}

	/** {@inheritDoc} */
	override fun isAvailable(): Boolean = this::agent.isInitialized

	/**
	 * Set an observer who will be notified at order-state update
	 */
	fun subscribeOrder(s: Observer<Order>) = orderObservers.add(s)

	/**
	 * Notify observers with a new order-state
	 */
	fun dispatchOrder(t: Order) { orderObservers.onEach { it.onNext(t) } }

	fun placeOrder(vararg items: Pair<String, Int>): Future<Boolean> = agent.placeOrder(*items)

	fun stop(): Future<Unit> = agent.shutdown()
}