package controlled.user.agent

import framework.Framework
import controller.user.agent.Agent as UserAgent
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.User.user
import framework.Framework.test
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert
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

    @Test fun shopItemShouldRequireAListOfItemsFromItemManager() = test { warehouseMapper
        agent(UserAgent::class.java).shopItems()
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("info(warehouse)", result.contentObject.toString())
    }

    @Test fun placeOrderShouldSendARequestToAnOrderManager() = test { orderManager
        agent(UserAgent::class.java).placeOrder(
            user(client("x"), email("y"), address("z")),
            listOf(
                item(id("a"), quantity(1)),
                item(id("b"), quantity(2))
            )
        )
        val result = orderManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals(
            """order(client("x"),email("y"),address("z"))[item(id("a"),quantity(1)),item(id("b"),quantity(2))]""",
            result.contentObject.toString()
        )
    }

    @Test fun ordersShouldSendARequestToAnOrderManager() = test { orderManager
        agent(UserAgent::class.java).orders(user(client("x"), email("y"), address("z")))
        val result = orderManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""info(client("x"),email("y"))""", result.contentObject.toString())
    }
}
