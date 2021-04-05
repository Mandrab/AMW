import jade.core.ProfileImpl
import jade.core.Runtime
import junit.framework.Assert.*
import org.junit.Test
import kotlin.random.Random
import jade.domain.DFService

import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import org.junit.Assert

class OrderManagerTest {
    private val proxy = proxy()

    @Test fun testerIsRegistering() = assertNotNull(proxy.agent)

    @Test fun agentExists() = Assert.assertTrue(proxy.agent.run {
        DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = "management(orders)"; type = "accept(order)" })
        })
    }.isNotEmpty())

    private fun proxy(): TestAgent.Proxy = TestAgent.Proxy().apply { Runtime.instance()
        .createAgentContainer(ProfileImpl()).createNewAgent("OrderManagerTest" + Random.nextDouble(),
        TestAgent().javaClass.canonicalName, arrayOf(this)).run { start() } }
}