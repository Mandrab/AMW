package controller.user.agent

import controller.user.agent.Proxy.Proxy
import jade.core.Agent

/**
 * Client-agent proxy class (proxy pattern)
 * It allows to communicate to agent through this
 *
 * @author Paolo Baldini
 */
class ClientProxy: Proxy {
	private lateinit var agent: ClientAgent

	/** {@inheritDoc} */
	override fun setAgent(agent: Agent) { this.agent = agent as ClientAgent }

	/** {@inheritDoc} */
	override fun isAvailable() = this::agent.isInitialized

	override fun placeOrder() = agent.placeOrder()

	override fun shutdown() = agent.shutdown()
}