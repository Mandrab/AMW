package tester.asl.order_manager

import framework.Framework.test
import framework.Framework.Utility.agent
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import framework.Framework.ASL
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.lang.acl.ACLMessage.INFORM
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Test
import org.junit.Assert

/**
 * Test class for OrderManager's orders info request
 *
 * @author Paolo Baldini
 */
class InfoOrderTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun infoRequestReturnsEmptyListIfNoOrderHasBeenMade() = test {
        agent .. REQUEST + info(client("a"), email("b")).term() > ASL.orderManager
        agent < INFORM + "[]"                                               // response check
    }

    @Test fun infoRequestGivesResultsIfOrdersHasBeenMade() = test {
        agent .. REQUEST + order(client("x"), email("y"), address("z"))[
                item(id("a"), quantity(2))
        ].term() > ASL.orderManager

        agent .. REQUEST + order(client("x"), email("y"), address("z"))[
                    item(id("a"), quantity(2)),
                    item(id("b"), quantity(2))
            ].term() > ASL.orderManager
        agent .. REQUEST + info(client("x"), email("y")).term() > ASL.orderManager

        agent < INFORM + "[order(id(odr2),status(check)),order(id(odr1),status(check))]"    // response check
    }
}
