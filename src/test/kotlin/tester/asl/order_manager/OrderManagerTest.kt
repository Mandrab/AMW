package tester.asl.order_manager

import jade.core.ProfileImpl
import jade.core.Runtime
import org.junit.Test
import kotlin.random.Random
import jade.domain.DFService

import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.CONFIRM
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert

class OrderManagerTest {
    private val _name = "management(orders)"
    private val _type = "accept(order)"
    private val waitingTime = 500L
    private val proxy = proxy()

    @Test fun testerIsRegistering() = Assert.assertNotNull(proxy.agent)

    @Test fun agentExists() = Assert.assertTrue(
        proxy.agent.run { DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = _name; type = _type })
        })}.isNotEmpty()
    )

    @Test fun withoutOrdersReturnsEmptyList() = proxy.agent.run {
        sendRequest("info('', '')")
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(result.content, "[]")
    }
/*
    @Test fun orderWithNoItemsIsIgnored() = proxy().agent.run {
        sendRequest("order('x', 'y', 'z')[]")
        Assert.assertNull(blockingReceive(waitingTime))
    }
*/
    @Test fun requestedOrderShouldBeConfirmed() {
        val agent = proxy().agent
        agent.sendRequest("order('x', 'y', 'z')[item, i]")
        val result = agent.blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(result.performative, CONFIRM)
        Assert.assertEquals(result.content, "order(client('x'), 'y', 'z')[item, i]")
    }

    private fun proxy(): TestAgent.Proxy = TestAgent.Proxy().apply { Runtime.instance()
        .createAgentContainer(ProfileImpl()).createNewAgent("tester.asl.order_manager.OrderManagerTest" + Random.nextDouble(),
        TestAgent().javaClass.canonicalName, arrayOf(this)).run { start() } }

    private fun TestAgent.sendRequest(content: String) = send(ACLMessage().also {
        it.addReceiver(DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = _name; type = _type })
        }).first().name)
        it.performative = REQUEST
        it.content = content
    })
}