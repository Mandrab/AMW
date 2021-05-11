package controlled.user.agent

import controller.user.agent.Agent as UserAgent
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.User.user
import framework.Framework.JADE
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.plus
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Test

/**
 * Test class for user agent
 * Some of the features are not tested here in that
 * they are (or will) be tested later in more adequate places
 *
 * @author Paolo Baldini
 */
class AgentTest {
    private val receiveWaitingTime = 1000L

    @Test fun shopItemShouldRequireAListOfItemsFromItemManager() = test {
        JADE.warehouseMapper
        agent(UserAgent::class.java).shopItems()

        JADE.warehouseMapper < REQUEST + "info(warehouse)"
    }

    @Test fun placeOrderShouldSendARequestToAnOrderManager() = test {
        JADE.orderManager
        agent(UserAgent::class.java).placeOrder(
            user(client("x"), email("y"), address("z")),
            listOf(
                item(id("a"), quantity(1)),
                item(id("b"), quantity(2))
            )
        )

        JADE.orderManager < REQUEST +
                """order(client("x"),email("y"),address("z"))[item(id("a"),quantity(1)),item(id("b"),quantity(2))]"""
    }

    @Test fun ordersShouldSendARequestToAnOrderManager() = test {
        JADE.orderManager
        agent(UserAgent::class.java).orders(user(client("x"), email("y"), address("z")))

        JADE.orderManager < REQUEST + """info(client("x"),email("y"))"""
    }
}
