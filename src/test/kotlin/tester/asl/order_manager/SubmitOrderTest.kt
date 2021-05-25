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
import framework.AMWSpecificFramework.oid
import framework.AMWSpecificFramework.retryTime
import framework.Messaging.compareTo
import framework.Messaging.lastMatches
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import jason.asSyntax.Literal
import org.junit.Assert
import java.util.jar.JarEntry

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
    private fun removeItemsRequest(mid: Int) = """remove(items,${oid(1)})[item(id("a"),quantity(1))""" +
            """,item(id("b"),quantity(2)),${mid(mid)}]"""
    private fun removeItemsResponse(mid: Int) =
        "remove(items,${lastMatches.first()})[${position(mid)},${position(mid+1)},${mid(mid)}]"
    private fun pointRequest(mid: Int) = """point(${oid(1)})[${mid(mid)}]"""
    private fun pointResponse(mid : Int) = "point(${lastMatches.first()}, pid, x, y)[${mid(mid)}]"
    private fun orderStatus(status: String) = "[order(id(${oid(1)}),status($status))]"
    private fun position(i: Int) = "position(x$i,y$i,z$i)"
    private fun error_(t: Literal) = "error($t)"
    private fun unknown_(t: String) = "unknown($t)"
    private val defaultOrder = order(client("x"), email("y"), address("z"))[
            item(id("a"), quantity(1)),
            item(id("b"), quantity(2))
    ].term()

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun orderWithNoItemsIsNotAccepted() = test {
        val order = order(client("x"), email("y"), address("z")).term()

        agent .. REQUEST + order > ASL.orderManager
        agent < FAILURE + unknown_(order.toString().removeSuffix("[]").trim())
    }

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = test {
        placeOrder() <= FAILURE + error_(defaultOrder)
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

        warehouseAcceptance()

        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + orderStatus("retrieve")
    }

    @Test fun orderInRetrievingCauseRequestToCollectionPointManager() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)
        warehouseAcceptance()

        JADE.collectionPointManager < REQUEST + "point(${oid(1)})[mid(mid2)]"
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromCollectionPointManager() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        Thread.sleep(retryTime)

        JADE.collectionPointManager <= REQUEST + pointRequest(2)
        JADE.collectionPointManager <= REQUEST + pointRequest(2)
    }

    @Test fun refuseFromCollectionPointManagerCauseAnotherRequest() = test {
        JADE.warehouseMapper; JADE.collectionPointManager

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        JADE.collectionPointManager <= REQUEST + pointRequest(2)
        JADE.collectionPointManager .. FAILURE + pointResponse(2) > ASL.orderManager

        Thread.sleep(retryTime)

        JADE.collectionPointManager < REQUEST + pointRequest(2)
    }

    @Test fun confirmFromCollectionPointManagerCauseStopOfRequest() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        val result = JADE.collectionPointManager.blockingReceive(retryTime + waitingTime)
        Assert.assertNull(result)
    }

    @Test fun confirmFromCollectionPointManagerCauseStartOfItemsPick() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker < REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker < REQUEST + retrieveItemMessage(2, 4)
    }

    @Test fun orderStatusShouldChangeWhenAllItemsAreRetrieved() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker < REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager

        ordersInfo() < INFORM + orderStatus("retrieve")
    }

    @Test fun orderStatusShouldChangeAfterLastElementIsRetrieved() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(2, 4) > ASL.orderManager

        ordersInfo() < INFORM + orderStatus("completed")
    }

    @Test fun freeOfCollectionPointShouldRepeatedlySentAfterLastElementIsRetrieved() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(2, 4) > ASL.orderManager

        JADE.collectionPointManager <= INFORM + "free(${oid(1)})[mid(mid5)]"

        Thread.sleep(retryTime)

        JADE.collectionPointManager <= INFORM + "free(${oid(1)})[mid(mid5)]"
    }

    @Test fun freeOfCollectionPointInformShouldStopAfterConfirmation() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(1, 3) > ASL.orderManager
        JADE.robotPicker <= CONFIRM + retrieveItemMessage(1, 3)

        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)
        JADE.robotPicker .. CONFIRM + retrieveItemMessage(2, 4) > ASL.orderManager
        JADE.robotPicker <= CONFIRM + retrieveItemMessage(2, 4)

        JADE.collectionPointManager <= INFORM + "free(${oid(1)})[mid(mid5)]"
        JADE.collectionPointManager .. CONFIRM + "free(${lastMatches.first()})[mid(mid5)]" > ASL.orderManager

        val result = JADE.collectionPointManager.blockingReceive(retryTime + waitingTime)
        Assert.assertNull(result)
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromRobotPicker() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker

        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
        JADE.robotPicker <= REQUEST + retrieveItemMessage(2, 4)

        Thread.sleep(retryTime)

        JADE.robotPicker <= REQUEST + retrieveItemMessage(1, 3)
    }

    @Test fun agentShouldConfirmRobotMessageReception() = test {
        JADE.warehouseMapper; JADE.collectionPointManager; JADE.robotPicker
        placeOrder().blockingReceive(waitingTime)

        warehouseAcceptance()

        collectionPointManagerAcceptance()

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

    private fun warehouseAcceptance() {
        JADE.warehouseMapper <= REQUEST + removeItemsRequest(1)
        JADE.warehouseMapper .. CONFIRM + removeItemsResponse(1) > ASL.orderManager
    }

    private fun collectionPointManagerAcceptance() {
        JADE.collectionPointManager <= REQUEST + pointRequest(2)
        JADE.collectionPointManager .. CONFIRM + pointResponse(2) > ASL.orderManager
    }
}
