package controller.agent

import jade.core.Agent

/**
 * An interface to be implemented by a proxy class (following proxy pattern)
 * It will allow to communicate with the agent through this
 *
 * @author Paolo Baldini
 */
interface AgentProxy {

	/**
	 * Set agent to be the proxy
	 */
	fun setAgent(agent: Agent)
}