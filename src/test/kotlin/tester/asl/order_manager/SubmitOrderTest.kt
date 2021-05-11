package tester.asl.order_manager

import framework.ASLAgent
import framework.Framework.ASL
import framework.Framework.JADE
import framework.Framework.Utility.agent
import framework.Framework.waitingTime
import framework.Framework.test
import framework.JADEAgent
import common.ontology.Services.ServiceType.*
import common.ontology.Services.ServiceSupplier.*
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
import framework.Framework
import framework.Messaging.compareTo
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
    private val defaultOrder = order(client("x"), email("y"), address("z"))[
            item(id("a"), quantity(1)),
            item(id("b"), quantity(2))
    ].term()

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun orderWithNoItemsIsNotAccepted() = test {
        agent .. REQUEST + order(client("x"), email("y"), address("z")).term() > ASL.orderManager

        val result = agent.blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(FAILURE, result.performative)
        Assert.assertTrue(result.content.startsWith("error(unknown,"))
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
        val orderManagerAID = ASL.orderManager.aid

        placeOrder(orderManagerAID)
        val result1 = JADE.warehouseMapper.blockingReceive(waitingTime)
        val result2 = JADE.warehouseMapper.blockingReceive(retryTime + waitingTime)

        assert(result1, INFORM, """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""")
        assert(result2, INFORM, """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""")
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
    }

    @Test fun orderCanBeRefusedIfTheWarehouseHasNotTheItems() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid

        val received = warehouseResponse(false)
        val client = placeOrder(orderManagerAID)
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        Thread.sleep(waitingTime)

        assert(getInfo(client, orderManagerAID), INFORM, "[order(id(odr1),status(refused))]")
    }

    @Test fun orderGetStatusRetrievingIfTheWarehouseHasTheItems() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid

        val received = warehouseResponse(true)
        val client = placeOrder(orderManagerAID)
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        Thread.sleep(waitingTime)

        assert(getInfo(client, orderManagerAID), INFORM, "[order(id(odr1),status(retrieve))]")
    }

    @Test fun orderInRetrievingCauseRequestToCollectionPointManager() = test {
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val received = warehouseResponse(true)
        val client = placeOrder()
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        val result = collectionPointManager.blockingReceive(waitingTime)
        collectionPointManager.deregister()

        assert(result, INFORM, "point")
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromCollectionPointManager() = test {
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val received = warehouseResponse(true)
        val client = placeOrder()

        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        val result1 = collectionPointManager.blockingReceive(waitingTime)
        val result2 = collectionPointManager.blockingReceive(retryTime + waitingTime)
        collectionPointManager.deregister()

        assert(result1, INFORM, "point")
        assert(result2, INFORM, "point")
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
    }

    @Test fun refuseFromCollectionPointManagerCauseAnotherRequest() = test {
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val received = warehouseResponse(true)
        val client = placeOrder()
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        waitAndReply(collectionPointManager, FAILURE)
        Thread.sleep(3000)                                      // wait time in orderManager
        val result = collectionPointManager.blockingReceive(waitingTime)
        collectionPointManager.deregister()

        assert(result, INFORM, "point")
    }

    @Test fun confirmFromCollectionPointManagerCauseStartOfItemsPick() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val robotPicker = agent().register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
        val received = warehouseResponse(true)
        val client = placeOrder(orderManagerAID)
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(collectionPointManager, CONFIRM, "point(pid)")
        collectionPointManager.deregister()

        val result1 = robotPicker.blockingReceive(waitingTime)
        val result2 = robotPicker.blockingReceive(waitingTime)
        robotPicker.deregister()

        assert(result1, INFORM, """retrieve(item(id("a"),quantity(1)),point(pid))""")
        assert(result2, INFORM, """retrieve(item(id("b"),quantity(2)),point(pid))""")
    }

    @Test fun orderStatusShouldChangeOnlyWhenAllItemsAreRetrieved() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val robotPicker = agent().register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
        val received = warehouseResponse(true)
        val client = placeOrder(orderManagerAID)
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(collectionPointManager, CONFIRM, "point(pid)")
        collectionPointManager.deregister()

        waitAndReply(robotPicker)
        robotPicker.blockingReceive(waitingTime)
        robotPicker.deregister()
        Thread.sleep(waitingTime)

        assert(getInfo(client, orderManagerAID), INFORM, """[order(id(odr1),status(retrieve))]""")
    }

    @Test fun orderStatusShouldChangeAfterLastElementIsRetrieved() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val robotPicker = agent().register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
        val received = warehouseResponse(true)
        val client = placeOrder(orderManagerAID)

        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)
        waitAndReply(collectionPointManager, CONFIRM, "point(pid)")
        collectionPointManager.deregister()

        waitAndReply(robotPicker)
        waitAndReply(robotPicker)
        robotPicker.deregister()
        Thread.sleep(waitingTime)

        assert(getInfo(client, orderManagerAID), INFORM, """[order(id(odr1),status(completed))]""")
    }

    @Test fun agentShouldKeepAskIfNoAnswerFromRobotPicker() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val collectionPointManager = agent().register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        val robotPicker = agent().register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
        val received = warehouseResponse(true)

        val client = placeOrder(orderManagerAID)
        client.blockingReceive(waitingTime)
        received.tryAcquire(waitingTime, TimeUnit.MILLISECONDS)

        waitAndReply(collectionPointManager, CONFIRM, "point(pid)")
        collectionPointManager.deregister()

        val result1 = robotPicker.blockingReceive(waitingTime)
        robotPicker.blockingReceive(waitingTime)
        val result2 = robotPicker.blockingReceive(retryTime + waitingTime)
        robotPicker.deregister()

        assert(result1, INFORM, """retrieve(item(id("a"),quantity(1)),point(pid))""")
        assert(result2, INFORM, """retrieve(item(id("a"),quantity(1)),point(pid))""")
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
    }

    private fun Framework.placeOrder(aid: AID = agent("order_manager", ASLAgent::class.java).aid) =
        agent().apply { sendRequest(defaultOrder, aid) }

    private fun Framework.warehouseResponse(confirmOrder: Boolean) = Semaphore(0).apply {
        val warehouse = agent().register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)
        warehouse.addBehaviour(oneShotBehaviour {
            waitAndReply(warehouse, if (confirmOrder) CONFIRM else FAILURE)
            warehouse.deregister()
            release(Int.MAX_VALUE)
        })
    }

    private fun getInfo(client: JADEAgent, orderManagerAID: AID) = client.sendRequest(
        info(client("x"), email("y")).term(), orderManagerAID
    ).blockingReceive(waitingTime)

    private fun waitAndReply(agent: Agent, performative: Int = CONFIRM, message: String? = null) {
        val result = agent.blockingReceive(waitingTime)
        agent.send(ACLMessage(performative).apply {
            addReceiver(result.sender)
            replyWith = result.inReplyTo
            content = message ?: result.content
        })
    }
}
