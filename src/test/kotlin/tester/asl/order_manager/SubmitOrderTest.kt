package tester.asl.order_manager

import common.ASLAgent
import common.Framework
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
import controller.agent.communication.translation.out.OperationTerms.term
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
 *
 * @author Paolo Baldini
 */
class SubmitOrderTest: Framework() {
    private val waitingTime = 500L

    @Test fun testerIsRegistering() = oneshotAgent(Assert::assertNotNull)

    @Test fun orderWithNoItemsIsIgnored() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val result = agent().sendRequest(
            order(client("x"), email("y"), address("z")).term(),
            orderManagerAID
        ).blockingReceive(waitingTime)
        Assert.assertNull(result)
    }

    @Test fun submittedOrderRequestShouldFailIfNoWarehouseAgentExists() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid

        val result = agent().sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2))
            ].term(), orderManagerAID
        ).blockingReceive(waitingTime)

        assert(result, FAILURE, """order(client("x"),email("y"),address("z"))[item(id("a"),quantity(2))]""")
    }

    @Test fun orderSubmissionShouldCauseRequestToWarehouseManager() = test {
        val warehouse = agent().register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid

        agent().sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(1)),
                    item(id("b"), quantity(2))
            ].term(), orderManagerAID
        ).blockingReceive(waitingTime)

        val result = warehouse.blockingReceive(waitingTime)
        warehouse.deregister()

        assert(result, INFORM_REF, """remove(items)[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""")
    }

    @Test fun submittedOrderReceptionShouldBeConfirmedIfRequiredAgentExists() = test {
        val warehouse = agent().register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid

        val result = agent().sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2))
            ].term(), orderManagerAID).blockingReceive(waitingTime)
        warehouse.deregister()

        assert(result, CONFIRM, """order(client("x"),email("y"),address("z"))[item(id("a"),quantity(2))]""")
    }

    @Test fun orderCanBeRefusedIfTheWarehouseHasNotTheItems() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val warehouse = agent().register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)
        val client = agent()

        client.sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(1)),
                    item(id("b"), quantity(2))
            ].term(), orderManagerAID
        ).blockingReceive(waitingTime)

        var result = warehouse.blockingReceive(waitingTime)
        warehouse.send(ACLMessage(FAILURE).apply {
            addReceiver(result.sender)
            content = result.content
            replyWith = result.inReplyTo
        })
        warehouse.deregister()

        result = client.sendRequest(
            info(client("x"), email("y")).term(), orderManagerAID
        ).blockingReceive(waitingTime)

        assert(result, INFORM, "[order(id(odr1),status(refused))]")
    }

    @Test fun orderGetStatusRetrievingIfTheWarehouseHasTheItems() = test {
        val orderManagerAID = agent("order_manager", ASLAgent::class.java).aid
        val warehouse = agent().register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id)
        val client = agent()

        client.sendRequest(
            order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(1)),
                    item(id("b"), quantity(2))
            ].term(), orderManagerAID
        ).blockingReceive(waitingTime)

        var result = warehouse.blockingReceive(waitingTime)
        warehouse.send(ACLMessage(CONFIRM).apply {
            addReceiver(result.sender)
            content = result.content
            replyWith = result.inReplyTo
        })
        warehouse.deregister()

        result = client.sendRequest(
            info(client("x"), email("y")).term(), orderManagerAID
        ).blockingReceive(waitingTime)

        assert(result, INFORM, "[order(id(odr1),status(retrieve))]")
    }
}