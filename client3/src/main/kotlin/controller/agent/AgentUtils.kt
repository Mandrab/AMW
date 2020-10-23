package controller.agent

import common.utils.ExceptionWrapper
import jade.core.ProfileImpl
import jade.core.Runtime

/**
 * Utility to start jade agents
 *
 * @author Paolo Baldini
 */
object AgentUtils {
	/**
	 * Try to start the jade agent
	 *
	 * @param agentClass the class to instantiate and start
	 * @param proxy proxy pattern to the agent
	 * @param retryConnection if true, retry a failed connection till it succeed
 	 */
	fun startAgent(agentClass: Class<*>, proxy: AgentProxy?, retryConnection: Boolean = true, vararg other: Any) {
		val agentName = "interface-ag%.10f".format(Math.random())
		val agentClassName = agentClass.canonicalName
		val objects = proxy?.let { arrayOf(it, *other) } ?: other

		fun startAgent() = ExceptionWrapper.ensure {
			Runtime.instance()											// get a hold on JADE runtime
				.createAgentContainer(ProfileImpl())					// create a container with default profile
				.createNewAgent(agentName, agentClassName, objects)		// Create a new agent
				.apply { start() }		 								// start the agent
		}

		startAgent() ?: if (retryConnection) startAgent()				// if an error occurred and need to retry, recur
			else throw Exception("Failed to initialize the agent")
	}
}