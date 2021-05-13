package tester.asl.order_manager

import framework.AMWSpecificFramework.ASL
import framework.AMWSpecificFramework.JADE
import framework.AMWSpecificFramework.waitingTime
import framework.Framework.Utility.agent
import framework.Framework.test
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import framework.AMWSpecificFramework.mid
import framework.AMWSpecificFramework.retryTime
import framework.Messaging
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for OrderManager's accept order request
 * core tests:
 *  - orders acceptance (correct and wrong format)
 *  - lack of required agent(s) in the system
 *  - lack of items
 *  - lack and wait for collection point manger
 *  - retrieval requests
 *  - missing message delivery / network errors
 *
 * @author Paolo Baldini
 */
class SubmitOrderTest {
    private fun retrieveItemMessage(i: Int, mid: Int) = "retrieve(${position(i)},point(pid))[${mid(mid)}]"
    private fun removeItemsRequest(mid: Int) = "remove(items,odr1)[" + """item(id("a"),quantity(1))""" +
            """,item(id("b"),quantity(2)),${mid(mid)}]"""
    private fun removeItemsResponse(mid: Int) = "remove(items,odr1)[${position(mid)},${position(mid+1)},${mid(mid)}]"
    private fun pointRequest(mid: Int) = "point(odr1)[${mid(mid)}]"
    private fun pointResponse(mid : Int) = "point(pid, x, y)[${mid(mid)}]"
    private fun orderStatus(status: String) = "[order(id(odr1),status($status))]"
    private fun position(i: Int) = "position(x$i,y$i,z$i)"
    private val defaultOrder = order(client("x"), email("y"), address("z"))[
            item(id("a"), quantity(1)),
            item(id("b"), quantity(2))
    ].term()

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun orderWithNoItemsIsNotAccepted() = test {
        val order = order(client("x"), email("y"), address("z")).term()

        agent .. REQUEST + order > ASL.orderManager
        agent < FAILURE + "unknown(${order.toString().removeSuffix("[]").trim()})"
    }

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = test {
        placeOrder() < FAILURE + defaultOrder
    }

    @Test fun orderSubmissionShouldCauseRequestToWarehouseManager() = test { JADE.warehouseMapper
        placeOrder().blockingReceive(waitingTime)

        JADE.warehouseMapper < REQUEST + removeItemsRequest(1)
    }

    @Test fun submittedOrderReceptionShouldBeConfirmedIfWarehouseManagerExists() = test { JADE.warehouseMapper
        placeOrder() < CONFIRM + defaultOrder
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromWarehouseManager() = test { JADE.warehouseMapper
        placeOrder()

        JADE.warehouseMapper < REQUEST + removeItemsRequest(1)

        Thread.sleep(retryTime)

        JADE.warehouseMapper < REQUEST + removeItemsRequest(1)
    }

    @Test fun orderCanBeRefusedIfTheWarehouseHasNotTheItems() = test { JADE.warehouseMapper
        placeOrder().blockingReceive(waitingTime)

        val result = JADE.warehouseMapper.blockingReceive(waitingTime)
        JADE.warehouseMapper .. FAILURE + result.content > ASL.orderManager

        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + orderStatus("refused")
    }

    @Test fun orderGetStatusRetrievingIfTheWarehouseHasTheItems() = test { JADE.warehouseMapper
        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + orderStatus("retrieve")
    }

    @Test fun orderInRetrievingCauseRequestToCollectionPointManager() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)
        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))

        JADE.collectionPointManager < REQUEST + "point"
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromCollectionPointManager() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))

        Thread.sleep(retryTime)

        JADE.collectionPointManager <= REQUEST + pointRequest(2)
        JADE.collectionPointManager <= REQUEST + pointRequest(2)
    }

    @Test fun refuseFromCollectionPointManagerCauseAnotherRequest() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, FAILURE)

        Thread.sleep(retryTime)

        JADE.collectionPointManager < REQUEST + pointRequest(2)
    }

    @Test fun confirmFromCollectionPointManagerCauseStartOfItemsPick() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, CONFIRM, pointResponse(2))

        JADE.robotPicker < REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker < REQUEST + retrieveItemMessage(2, 4)
    }

    @Test fun orderStatusShouldChangeWhenAllItemsAreRetrieved() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, CONFIRM, pointResponse(2))

        JADE.robotPicker < REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager

        ordersInfo() < INFORM + orderStatus("retrieve")
    }

    @Test fun orderStatusShouldChangeAfterLastElementIsRetrieved() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, CONFIRM, pointResponse(2))

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(2, 4) > ASL.orderManager

        ordersInfo() < INFORM + orderStatus("completed")
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromRobotPicker() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, CONFIRM, pointResponse(2))

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        Thread.sleep(retryTime)

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
    }

    @Test fun agentShouldConfirmRobotMessageReception() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker
        placeOrder().blockingReceive(waitingTime)

        waitAndReply(JADE.warehouseMapper, CONFIRM, removeItemsResponse(1))
        waitAndReply(JADE.collectionPointManager, CONFIRM, pointResponse(2))

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(2, 4) > ASL.orderManager

        JADE.robotPicker <= CONFIRM + retrieveItemMessage(1, 3)
        JADE.robotPicker <= CONFIRM + retrieveItemMessage(2, 4)

        val result = JADE.robotPicker.blockingReceive(retryTime + waitingTime)
        Assert.assertNull(result)
    }

    private fun placeOrder() = agent.apply { this .. REQUEST + defaultOrder > ASL.orderManager }

    private fun ordersInfo() = agent.apply {
        this .. REQUEST + info(client("x"), email("y")).term() > ASL.orderManager
    }

    private fun waitAndReply(agent: Agent, performative: Int = CONFIRM, message: String? = null) {
        val result = agent.blockingReceive(waitingTime)
        agent.send(ACLMessage(performative).apply {
            addReceiver(result.sender)
            content = message ?: result.content
        })
    }

    private operator fun Agent.compareTo(message: Messaging.Message) = 0.apply {
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(message.performative, result.performative)
        message.content
            ?.let { Assert.assertTrue(result.content.contains(it)) }
            ?: Assert.assertTrue(result.content.contains(message.contentObject.toString().trim()))
        message.replyWith ?.let { Assert.assertEquals(it, result.inReplyTo) }
        message.mid ?.let { it.value = result.inReplyTo }
    }
}
