package tester.asl.order_manager

import common.ASLAgents
import common.ASLAgents.start
import common.JADEAgents.TestProxy
import common.JADEAgents.proxy
import common.JADEAgents.register
import common.JADEAgents.shutdown
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import jade.core.AID
import jade.core.Agent
import org.junit.Test
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.*
import org.junit.AfterClass
import org.junit.Assert

class OrderManagerTest {
    companion object {
        private const val waitingTime = 500L
        private val agent = proxy().agent

        @AfterClass fun terminate() {
            ASLAgents.killAll()
            agent.doDelete()
        }

        private fun proxy(): TestProxy<Agent> = proxy("tester.asl.order_manager.OrderManagerTest")
    }

    @Test fun testerIsRegistering() = Assert.assertNotNull(agent)

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = agent.run {
        sendRequest("info('', '')")
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals("[]", result.content)
    }

    @Test fun orderWithNoItemsIsIgnored() = proxy().agent.apply {
        sendRequest(order(client("x"), email("y"), address("z")).term().toString())
        Assert.assertNull(blockingReceive(waitingTime))
    }.doDelete()

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = proxy().agent.apply {
        sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2))
            ].term().toString()
        )
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(FAILURE, result.performative)
    }.doDelete()

    @Test fun submittedOrderShouldBeConfirmedIfRequiredAgentExists() = proxy().agent.apply {
        val picker = proxy("warehouse_mapper").agent.apply {
            register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)                   // order manager reject without mapper agent
        }
        val message = order(client("x"), email("y"), address("z"))[
                item(id("a"), quantity(2))
        ].term().toString()
        sendRequest(message)
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(CONFIRM, result.performative)
        Assert.assertEquals(message, result.content)
        picker.shutdown()
    }.doDelete()

    private fun orderManagerAID(): AID = start("order_manager")

    private fun Agent.sendRequest(message: String) = send(ACLMessage().apply {
        addReceiver(orderManagerAID())                                      // start a new agent every call
        performative = REQUEST
        content = message
    })
}
