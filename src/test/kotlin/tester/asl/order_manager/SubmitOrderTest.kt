package tester.asl.order_manager

import framework.Framework.ASL
import framework.Framework.JADE
import framework.Framework.Utility.agent
import framework.Framework.waitingTime
import framework.Framework.test
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.Agents.oneShotBehaviour
import controller.agent.communication.translation.out.OperationTerms.term
import framework.Messaging
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

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
    private val itemA = """item(id("a"),quantity(1))"""
    private val itemB = """item(id("b"),quantity(2))"""
    private val mid = mid(1)
    private fun mid(i: Int) = "mid(mid$i)"
    private val oid = "odr1"
    private val retrieveItemMessage1 = """retrieve(item(id("a"),quantity(1)),point(pid))[${mid(3)}]"""
    private val retrieveItemMessage2 = """retrieve(item(id("b"),quantity(2)),point(pid))[${mid(4)}]"""
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

        JADE.warehouseMapper < REQUEST + "remove(items,$oid)[$itemA,$itemB,$mid]"
    }

    @Test fun submittedOrderReceptionShouldBeConfirmedIfWarehouseManagerExists() = test { JADE.warehouseMapper
        placeOrder() < CONFIRM + defaultOrder
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromWarehouseManager() = test { JADE.warehouseMapper
        placeOrder()

        JADE.warehouseMapper < REQUEST + "remove(items,$oid)[$itemA,$itemB,$mid]"

        Thread.sleep(retryTime)

        JADE.warehouseMapper < REQUEST + "remove(items,$oid)[$itemA,$itemB,$mid]"
    }

    @Test fun orderCanBeRefusedIfTheWarehouseHasNotTheItems() = test {
        val received = warehouseResponse(false)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + "[order(id(odr1),status(refused))]"
    }

    @Test fun orderGetStatusRetrievingIfTheWarehouseHasTheItems() = test {
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)

        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + "[order(id(odr1),status(retrieve))]"
    }

    @Test fun orderInRetrievingCauseRequestToCollectionPointManager() = test { JADE.collectionPointManager
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        JADE.collectionPointManager < REQUEST + "point"
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromCollectionPointManager() = test { JADE.collectionPointManager
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        Thread.sleep(retryTime)

        JADE.collectionPointManager <= REQUEST + "point($oid)[${mid(2)}]"
        JADE.collectionPointManager <= REQUEST + "point($oid)[${mid(2)}]"
    }

    @Test fun refuseFromCollectionPointManagerCauseAnotherRequest() = test { JADE.collectionPointManager
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, FAILURE)
        Thread.sleep(3000)                                      // wait time in orderManager

        JADE.collectionPointManager < REQUEST + "point($oid)[${mid(2)}]"
    }

    @Test fun confirmFromCollectionPointManagerCauseStartOfItemsPick() = test {
        JADE.collectionPointManager; JADE.robotPicker

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)[${mid(2)}]")

        JADE.robotPicker < REQUEST + """retrieve($itemA,point(pid))"""
        JADE.robotPicker < REQUEST + """retrieve($itemB,point(pid))"""
    }

    @Test fun orderStatusShouldChangeWhenAllItemsAreRetrieved() = test { JADE.collectionPointManager; JADE.robotPicker
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)[${mid(2)}]")

        waitAndReply(JADE.robotPicker)
        JADE.robotPicker.blockingReceive(waitingTime)
        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + "[order(id($oid),status(retrieve))]"
    }

    @Test fun orderStatusShouldChangeAfterLastElementIsRetrieved() = test {
        JADE.collectionPointManager; JADE.robotPicker

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)[${mid(2)}]")

        JADE.robotPicker <= REQUEST + retrieveItemMessage1
        JADE.robotPicker <= REQUEST + retrieveItemMessage2

        JADE.robotPicker .. CONFIRM + retrieveItemMessage1 > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage2 > ASL.orderManager

        ordersInfo() < INFORM + "[order(id($oid),status(completed))]"
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromRobotPicker() = test { JADE.collectionPointManager; JADE.robotPicker
        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)[${mid(2)}]")

        JADE.robotPicker <= REQUEST + retrieveItemMessage1
        JADE.robotPicker <= REQUEST + retrieveItemMessage2

        Thread.sleep(retryTime)

        JADE.robotPicker <= REQUEST + retrieveItemMessage1
    }

    @Test fun agentShouldConfirmRobotMessageReception() = test { JADE.collectionPointManager; JADE.robotPicker
        val receivedRequest = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        receivedRequest.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)[${mid(2)}]")

        JADE.robotPicker <= REQUEST + retrieveItemMessage1
        JADE.robotPicker <= REQUEST + retrieveItemMessage2

        JADE.robotPicker .. CONFIRM + retrieveItemMessage1 > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage2 > ASL.orderManager

        JADE.robotPicker <= CONFIRM + retrieveItemMessage1
        JADE.robotPicker <= CONFIRM + retrieveItemMessage2

        val result = JADE.robotPicker.blockingReceive(retryTime + waitingTime).apply { println(this) }
        Assert.assertNull(result)
    }

    private fun placeOrder() = agent.apply { this .. REQUEST + defaultOrder > ASL.orderManager }

    private fun warehouseResponse(confirmOrder: Boolean) = Semaphore(0).apply {
        JADE.warehouseMapper.addBehaviour(oneShotBehaviour {
            waitAndReply(JADE.warehouseMapper, if (confirmOrder) CONFIRM else FAILURE)
            release(Int.MAX_VALUE)
        })
    }

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
        message.content.apply { println(this) }
            ?.let { Assert.assertTrue(result.content.apply { println(this) }.contains(it)) }
            ?: Assert.assertTrue(result.content.contains(message.contentObject.toString().trim()))
        message.replyWith ?.let { Assert.assertEquals(it, result.inReplyTo) }
        message.mid ?.let { it.value = result.inReplyTo }
    }
}
