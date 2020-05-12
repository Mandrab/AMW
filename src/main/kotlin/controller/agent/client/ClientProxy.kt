package controller.agent.client

import common.Request
import common.Request.END
import common.Request.ORDER
import controller.agent.abstracts.ItemUpdaterProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.functions.Consumer
import jade.core.Agent
import model.Order

/**
 * Client-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this
 *
 * @author Paolo Baldini
 */
class ClientProxy: ItemUpdaterProxy(), Consumer<Pair<Request, Array<out Any>>> {
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

	/** {@inheritDoc} */
	override fun accept(t: Pair<Request, Array<out Any>>) {
		when (t.first) {
			ORDER -> if (t.second.isNotEmpty() && t.second[0] is String)
					agent.placeOrder(t.second[0] as String)
			END -> agent.shutdown()
			else -> Unit
		}
	}
}