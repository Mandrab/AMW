package common

import jade.core.AID
import jade.core.Agent
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.util.ExtendedProperties
import jade.wrapper.AgentContainer
import kotlin.collections.HashMap
import kotlin.random.Random

object ASLAgents {
    private val agents = HashMap<String, AgentContainer>()

    fun start(applicant: Agent, agentName: String, serviceName: String, vararg serviceTypes: String): AID =
        agentName.unique().let { name ->
            val monitor = ASLAgent.AIDMonitor()                             // to get the agent aid

            val props = ExtendedProperties()                                // define container properties
            props["main"] = "false"                                         // it is not the main jade container

            // start the agent
            agents[agentName] ?.kill()
            agents[agentName] = Runtime.instance().run {
                setCloseVM(true)
                createAgentContainer(ProfileImpl(props))
            }.apply {
                createNewAgent(
                    name,
                    ASLAgent().javaClass.name,
                    listOf("src/main/asl/$agentName.asl", monitor).toTypedArray()
                ).start()
            }

            return monitor.aid
        }

    private fun String.unique() = this + Random.nextDouble()
}
