import jade.core.ProfileImpl
import jade.core.Runtime
import org.junit.Test
import kotlin.random.Random
import jade.domain.DFService

import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert

class OrderManagerTest {
    private val _name = "management(orders)"
    private val _type = "accept(order)"
    private val proxy = proxy()

    @Test fun testerIsRegistering() = Assert.assertNotNull(proxy.agent)

    @Test fun agentExists() = Assert.assertTrue(
        proxy.agent.run { DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = _name; type = _type })
        })}.isNotEmpty()
    )

    @Test fun withoutOrdersReturnsEmptyList() {
        val agent = proxy().agent
        agent.send(ACLMessage().also {
            it.addReceiver(DFService.search(agent, DFAgentDescription().apply {
                addServices(ServiceDescription().apply { name = _name; type = _type })
            }).first().name)
            it.performative = REQUEST
            it.content = "info('', '')"
        })
        val result = agent.blockingReceive(1000)
        Assert.assertNotNull(result)
        Assert.assertEquals(result.content, "[]")
    }

    private fun proxy(): TestAgent.Proxy = TestAgent.Proxy().apply { Runtime.instance()
        .createAgentContainer(ProfileImpl()).createNewAgent("OrderManagerTest" + Random.nextDouble(),
        TestAgent().javaClass.canonicalName, arrayOf(this)).run { start() } }
}