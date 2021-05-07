package common

import jade.core.ProfileImpl
import jade.core.Runtime
import jade.util.ExtendedProperties
import jade.wrapper.AgentController
import utility.ExceptionWrapper.ensure
import kotlin.collections.HashMap
import kotlin.random.Random

object ASLAgents {
    private val agents = HashMap<String, AgentController>()

    fun start(agentName: String): ASLAgent = agentName.unique().let { name ->
        val monitor = ASLAgent.AIDMonitor()                                 // to get the agent aid

        val props = ExtendedProperties()                                    // define container properties
        props["main"] = "false"                                             // it is not the main jade container

        // start the agent
        ensure {
            agents[agentName] ?.kill()
        }
        agents[agentName] = Runtime.instance().run {
            setCloseVM(true)
            createAgentContainer(ProfileImpl(props)).createNewAgent(
                name,
                ASLAgent().javaClass.name,
                listOf("src/main/asl/$agentName.asl", monitor).toTypedArray()
            ).apply { start() }
        }

        return monitor.agent
    }

    fun killAll() = agents.forEach { it.value.kill() }

    private fun String.unique() = this + Random.nextDouble()
}
