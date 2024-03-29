package controller.agent

import jade.core.ProfileImpl
import jade.core.Runtime
import jade.core.behaviours.Behaviour
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.OneShotBehaviour
import utility.ExceptionWrapper

/**
 * Utilities for jade agents
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
    val start = { retry: Boolean -> { others: Array<Any> -> { agentClass: Class<*> ->
        val agentName = "interface-ag%.10f".format(Math.random())
        val agentClassName = agentClass.canonicalName

        fun startAgent() = ExceptionWrapper.ensure {
            Runtime.instance()                                          // get a hold on JADE runtime
                .createAgentContainer(ProfileImpl())                    // create a container with default profile
                .createNewAgent(agentName, agentClassName, others)      // create a new agent
                .apply { start() }                                      // start the agent
        }

        // if an error occurred and need to retry, recur
        startAgent() ?: if (retry) startAgent() else throw Exception("Failed to initialize the agent")
    } } }

    fun oneShotBehaviour(operation: (behaviour: Behaviour) -> Unit) = object: OneShotBehaviour() {
        override fun action() = operation(this)
    }

    fun cyclicBehaviour(operation: (behaviour: Behaviour) -> Unit) = object: CyclicBehaviour() {
        override fun action() = operation(this)
    }
}
