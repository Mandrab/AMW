package controller.agent

import com.sun.tools.attach.AgentInitializationException
import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.AgentController
import jade.wrapper.ContainerController
import jade.wrapper.StaleProxyException

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
		var agent: AgentController? = null
		do {
			try {
				val rt = Runtime.instance()                                 // get a hold on JADE runtime
				val p: Profile = ProfileImpl()                              // create a default profile
				val cc: ContainerController = rt.createAgentContainer(p)    // create a new non-main container
				agent = cc.createNewAgent(                                  // Create a new agent
					"interface-ag" + String.format("%.10f", Math.random()),
					agentClass.canonicalName, proxy?.let { arrayOf(it, *other) } ?: emptyArray()
				)
				agent.start()                                               // fire up the agent
			} catch (se: StaleProxyException) {
				throw AgentInitializationException("Failed to initialize the agent")
			} catch (npe: NullPointerException) {                           // when the system is not running
				Thread.sleep(1000)
				if (!retryConnection) npe.printStackTrace()
			}
		} while (agent == null && retryConnection)                          // while an error occurred and need to retry
	}
}