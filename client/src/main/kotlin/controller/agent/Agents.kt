package controller.agent

import jade.core.ProfileImpl
import jade.core.Runtime
import utility.ExceptionWrapper

/**
 * Utility to start jade agents
 *
 * @author Paolo Baldini
 */
object Agents {

    /**
     * Try to start the jade agent
     *
     * @param retry if true, retry a failed connection till it succeed
     * @param agentClass the class to instantiate and start
     * @param others objects to pass to the agent
     */
    val start = { retry: Boolean -> { others: Array<Any> -> { agentClass: Class<*> -> {
        val agentName = "interface-ag%.10f".format(Math.random())
        val agentClassName = agentClass.canonicalName

        fun startAgent() = ExceptionWrapper.ensure {
            Runtime.instance()                                            // get a hold on JADE runtime
                .createAgentContainer(ProfileImpl())                    // create a container with default profile
                .createNewAgent(agentName, agentClassName, others)        // Create a new agent
                .apply { start() }                                        // start the agent
        }

        // if an error occurred and need to retry, recur
        startAgent() ?: if (retry) startAgent() else throw Exception("Failed to initialize the agent")
    } } } }
}