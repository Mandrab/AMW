package tester.asl.order_manager

import common.ASLAgent
import common.ASLAgents.start
import common.Framework.Companion.test
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import jade.lang.acl.ACLMessage.INFORM
import org.junit.Test
import org.junit.Assert

/**
 * Test class for OrderManager's orders info request
 *
 * @author Paolo Baldini
 */
class InfoOrderTest {
    private val waitingTime = 500L

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = test { agent()() {
        sendRequest(
            info(client("a"), email("b")).term(),
            agent("order_manager", ASLAgent::class.java).aid
        )
        val result = blockingReceive(waitingTime)
        assert(result, INFORM, "[]")
    } }

    @Test fun infoRequestGivesResultsIfOrdersHasBeenMade() = test { agent()() {
        start("order_manager").let {
            sendRequest(
                order(client("x"), email("y"), address("z"))[
                        item(id("a"), quantity(2))
                ].term(), it.aid
            )
            sendRequest(
                order(client("x"), email("y"), address("z"))[
                        item(id("a"), quantity(2)),
                        item(id("b"), quantity(2))
                ].term(), it.aid
            )
            sendRequest(
                info(client("x"), email("y")).term(), it.aid
            )
        }
        val result = blockingReceive(waitingTime)
        assert(result, INFORM, "[order(id(odr2),status(check)),order(id(odr1),status(check))]")
    } }
}
