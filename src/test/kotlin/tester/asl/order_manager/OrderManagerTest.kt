package tester.asl.order_manager

import common.ASLAgents.start
import common.Framework
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
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

class OrderManagerTest: Framework() {
    private val waitingTime = 500L
    private val agent = agent()

    @Test fun testerIsRegistering() = Assert.assertNotNull(agent)

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = agent {
        sendRequest("info('', '')", orderManagerAID())
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals("[]", result.content)
    }

    @Test fun infoRequestGivesResultsIfOrdersHasBeenMade() = agent {//TODO
        sendRequest("info('', '')", orderManagerAID())
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals("[]", result.content)
    }

    @Test fun orderWithNoItemsIsIgnored() = oneshotAgent {
        sendRequest(order(client("x"), email("y"), address("z")).term().toString(), orderManagerAID())
        Assert.assertNull(blockingReceive(waitingTime))
    }

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = oneshotAgent {
        sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2))
            ].term().toString(),
            orderManagerAID()
        )
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(FAILURE, result.performative)
    }

    @Test fun submittedOrderShouldBeConfirmedIfRequiredAgentExists() = oneshotAgent {
        register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)   // act as WarehouseMapper: OrderManager reject orders without it
        oneshotAgent {
            val message = order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2))
            ].term().toString()
            sendRequest(message, orderManagerAID())
            val result = blockingReceive(waitingTime)
            Assert.assertNotNull(result)
            Assert.assertEquals(CONFIRM, result.performative)
            Assert.assertEquals(message, result.content)
        }
        deregister()
    }

    private fun orderManagerAID(): AID = start("order_manager")
}
