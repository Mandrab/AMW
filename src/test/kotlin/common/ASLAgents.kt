package common

import jade.core.AID
import jade.core.Agent
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.util.ExtendedProperties
import jade.wrapper.AgentContainer
import java.util.concurrent.TimeoutException
import kotlin.random.Random

object ASLAgents {
    private const val waitTime = 500L

    private val agents = HashMap<String, AgentContainer>()

    fun start(applicant: Agent, agentName: String, serviceName: String, vararg serviceTypes: String): AID =
        agentName.unique().let { name ->
            // define asl to start
            val props = ExtendedProperties()
            props["main"] = "false"
            props["agents"] = "$name:jason.infra.jade.JadeAgArch(src/main/asl/$agentName.asl)"

            // start the agent
            agents[agentName] ?.kill()
            agents[agentName] = Runtime.instance().run {
                setCloseVM(true)
                createAgentContainer(ProfileImpl(props))
            }

            return aid(applicant, 5, name, serviceName, *serviceTypes)
        }

    private tailrec fun aid(applicant: Agent, count: Int, agentName: String, service: String, vararg types: String): AID {
        val aids = DFService.search(applicant, DFAgentDescription().apply {
            types.forEach { addServices(ServiceDescription().apply { name = service; type = it }) }
        })
        if (aids.isEmpty()) {
            if (count == 0)
                throw TimeoutException("$agentName not found in DF")
            Thread.sleep(waitTime)
            return aid(applicant, count -1, agentName, service, *types)
        }
        return aids.first().name
    }

    private fun String.unique() = this + Random.nextDouble()
}
