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
import framework.Framework.Utility.mid
import framework.Messaging
import framework.Messaging.compareTo
import framework.Messaging.minus
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.core.AID
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
    private val retrieveItemMessage1 = """retrieve(item(id("a"),quantity(1)),point(pid))"""
    private val retrieveItemMessage2 = """retrieve(item(id("b"),quantity(2)),point(pid))"""
    private val defaultOrder = order(client("x"), email("y"), address("z"))[
            item(id("a"), quantity(1)),
            item(id("b"), quantity(2))
    ].term()

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun orderWithNoItemsIsNotAccepted() = test {
        agent .. REQUEST + order(client("x"), email("y"), address("z")).term() > ASL.orderManager
        agent < FAILURE + "error(unknown,"
    }

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = test {
        placeOrder() < FAILURE + defaultOrder
    }

    @Test fun orderSubmissionShouldCauseRequestToWarehouseManager() = test {
        JADE.warehouseMapper
        placeOrder().blockingReceive(waitingTime)
        JADE.warehouseMapper < INFORM + """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]"""
    }

    @Test fun submittedOrderReceptionShouldBeConfirmedIfWarehouseManagerExists() = test {
        JADE.warehouseMapper
        placeOrder() < CONFIRM + defaultOrder
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromWarehouseManager() = test {
        JADE.warehouseMapper
        placeOrder()

        val mid1 = Messaging.MID()
        JADE.warehouseMapper < INFORM + """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""" - mid

        Thread.sleep(retryTime)

        JADE.warehouseMapper < INFORM + """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""" - mid1
        Assert.assertEquals(mid.value, mid1.value)
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

    @Test fun orderInRetrievingCauseRequestToCollectionPointManager() = test {
        JADE.collectionPointManager

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        JADE.collectionPointManager < INFORM + "point"
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromCollectionPointManager() = test {
        JADE.collectionPointManager

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        val result1 = JADE.collectionPointManager.blockingReceive(waitingTime)
        val result2 = JADE.collectionPointManager.blockingReceive(retryTime + waitingTime)

        assert(result1, INFORM, "point")
        assert(result2, INFORM, "point")
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
    }

    @Test fun refuseFromCollectionPointManagerCauseAnotherRequest() = test {
        JADE.collectionPointManager

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        waitAndReply(JADE.collectionPointManager, FAILURE)
        Thread.sleep(3000)                                      // wait time in orderManager

        JADE.collectionPointManager < INFORM + "point"
    }

    @Test fun confirmFromCollectionPointManagerCauseStartOfItemsPick() = test {
        JADE.collectionPointManager
        JADE.robotPicker

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)")

        JADE.robotPicker < INFORM + """retrieve(item(id("a"),quantity(1)),point(pid))"""
        JADE.robotPicker < INFORM + """retrieve(item(id("b"),quantity(2)),point(pid))"""
    }

    @Test fun orderStatusShouldChangeOnlyWhenAllItemsAreRetrieved() = test {
        JADE.collectionPointManager
        JADE.robotPicker

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)")

        waitAndReply(JADE.robotPicker)
        JADE.robotPicker.blockingReceive(waitingTime)
        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + """[order(id(odr1),status(retrieve))]"""
    }

    @Test fun orderStatusShouldChangeAfterLastElementIsRetrieved() = test {
        JADE.collectionPointManager; JADE.robotPicker

        val received = warehouseResponse(true)
        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)")

        val mid1 = Messaging.MID()
        JADE.robotPicker <= INFORM + retrieveItemMessage1 - mid
        JADE.robotPicker <= INFORM + retrieveItemMessage2 - mid1

        JADE.robotPicker .. CONFIRM + retrieveItemMessage1 - mid > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItemMessage2 - mid1 > ASL.orderManager

        Thread.sleep(waitingTime)

        ordersInfo() < INFORM + """[order(id(odr1),status(completed))]"""
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromRobotPicker() = test {
        JADE.collectionPointManager
        JADE.robotPicker

        val received = warehouseResponse(true)

        placeOrder().blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)")

        JADE.robotPicker <= INFORM + retrieveItemMessage1 - mid
        JADE.robotPicker <= INFORM + retrieveItemMessage2

        Thread.sleep(retryTime)

        val mid1 = Messaging.MID()
        JADE.robotPicker <= INFORM + retrieveItemMessage1 - mid1

        Assert.assertEquals(mid.value, mid1.value)
    }

    @Test fun agentShouldConfirmRobotMessageReception() = test { JADE.collectionPointManager; JADE.robotPicker
        val retrieveItem1 = """retrieve(item(id("a"),quantity(1)),point(pid))"""
        val retrieveItem2 = """retrieve(item(id("b"),quantity(2)),point(pid))"""

        val receivedRequest = warehouseResponse(true)

        placeOrder().blockingReceive(waitingTime)
        receivedRequest.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(JADE.collectionPointManager, CONFIRM, "point(pid)")

        val mid1 = Messaging.MID()
        JADE.robotPicker <= INFORM + retrieveItem1 - mid
        JADE.robotPicker <= INFORM + retrieveItem2 - mid1

        JADE.robotPicker .. CONFIRM + retrieveItem1 - mid > ASL.orderManager
        JADE.robotPicker .. CONFIRM + retrieveItem2 - mid1 > ASL.orderManager

        JADE.robotPicker <= CONFIRM + retrieveItem1
        JADE.robotPicker <= CONFIRM + retrieveItem2

        val result = JADE.robotPicker.blockingReceive(retryTime + waitingTime)
        Assert.assertNull(result)
    }

    private fun placeOrder(aid: AID = ASL.orderManager.aid) = agent.apply { sendRequest(defaultOrder, aid) }

    private fun warehouseResponse(confirmOrder: Boolean) = Semaphore(0).apply {
        JADE.warehouseMapper.addBehaviour(oneShotBehaviour {
            waitAndReply(JADE.warehouseMapper, if (confirmOrder) CONFIRM else FAILURE)
            JADE.warehouseMapper.deregister()
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
            replyWith = result.inReplyTo
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
