package controller.agent

import jade.core.Agent

/**
 * An interface to be implemented by a proxy class (following proxy pattern)
 * It will allow to communicate with the agent through this
 *
 * @author Paolo Baldini
 */
interface AgentProxy<T: Agent> {

	/**
	 * Set agent to be the proxy of
	 */
	fun setAgent(agent: T)

	/**
	 * Says if proxy is set and available
	 */
	fun isAvailable(): Boolean

	/**
	 * Shutdown the agent
	 */
	fun shutdown()
}
