package tester.asl.order_manager

import common.ASLAgents.start
import common.JADEAgents.TestProxy
import common.JADEAgents.proxy
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import jade.core.AID
import jade.core.Agent
import org.junit.Test
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.CONFIRM
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert

class OrderManagerTest {
    private val waitingTime = 500L
    private val agent = proxy().agent

    @Test fun testerIsRegistering() = Assert.assertNotNull(agent)

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = agent.run {
        sendRequest("info('', '')")
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals("[]", result.content)
    }

    @Test fun orderWithNoItemsIsIgnored() = proxy().agent.run {
        sendRequest("order('x', 'y', 'z')[]")
        Assert.assertNull(blockingReceive(waitingTime))
    }

    @Test fun submittedOrderShouldBeConfirmed() {
        val agent = proxy().agent
        agent.sendRequest("order('x', 'y', 'z')[item, i]")
        val result = agent.blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(CONFIRM, result.performative)
        Assert.assertEquals("order(client('x'), 'y', 'z')[item, i]", result.content)
    }

    private fun proxy(): TestProxy<Agent> = proxy("tester.asl.order_manager.OrderManagerTest")

    private fun orderManagerAID(): AID = start(agent, "order_manager", MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id)

    private fun Agent.sendRequest(content: String) = send(ACLMessage().also {
        it.addReceiver(orderManagerAID())
        it.performative = REQUEST
        it.content = content
    })
}
