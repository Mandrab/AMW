package tester.asl.order_manager

import common.TestAgent
import common.TestAgents.TestProxy
import common.TestAgents.proxy
import jade.core.Agent
import org.junit.Test
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
    private val agent = proxy().agent

    @Test fun testerIsRegistering() = Assert.assertNotNull(agent)

    @Test fun orderManagerExists() = Assert.assertTrue(agent.run {
        DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = _name; type = _type })
        })}.isNotEmpty())

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = agent.run {
        sendRequest("info('', '')")
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(result.content, "[]")
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
        Assert.assertEquals(result.performative, CONFIRM)
        Assert.assertEquals(result.content, "order(client('x'), 'y', 'z')[item, i]")
    }

    private fun proxy(): TestProxy<Agent> = proxy("tester.asl.order_manager.OrderManagerTest")

    private fun Agent.sendRequest(content: String) = send(ACLMessage().also {
        it.addReceiver(DFService.search(this, DFAgentDescription().apply {
            addServices(ServiceDescription().apply { name = _name; type = _type })
        }).first().name)
        it.performative = REQUEST
        it.content = content
    })
}
