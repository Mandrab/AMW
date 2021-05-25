package framework

import jade.core.ProfileImpl
import jade.core.Runtime
import jade.util.ExtendedProperties
import kotlin.random.Random

/**
 * Utilities for the start of an asl agent from it's main file
 *
 * @author Paolo Baldini
 */
object ASLAgents {

    fun start(agentName: String): ASLAgent = agentName.unique().let { name ->
        val monitor = ASLAgent.AIDMonitor()                                 // to get the agent aid

        val props = ExtendedProperties()                                    // define container properties
        props["main"] = "false"                                             // it is not the main jade container

        // start the agent
        Runtime.instance().run {
            setCloseVM(true)
            createAgentContainer(ProfileImpl(props)).createNewAgent(
                name,
                ASLAgent().javaClass.name,
                listOf("src/main/asl/$agentName.asl", monitor).toTypedArray()
            ).apply { start() }
        }

        return monitor.agent
    }

    private fun String.unique() = this + Random.nextDouble()
}
