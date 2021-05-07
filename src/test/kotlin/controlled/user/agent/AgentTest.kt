package controlled.user.agent

import common.Framework
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
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

/**
 * Test class for user agent
 * Some of the features are not tested here in that
 * they are (or will) be tested later in more adequate places
 *
 * @author Paolo Baldini
 */
class AgentTest: Framework() {
    private val receiveWaitingTime = 1000L

    private val userAgent = agent(UserAgent::class.java)
    private val warehouseMapper = agent().register(MANAGEMENT_ITEMS.id, INFO_WAREHOUSE.id)
    private val orderManager = agent().register(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id, INFO_ORDERS.id)

    @Test fun shopItemShouldRequireAListOfItemsFromItemManager() {
        userAgent.shopItems()
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("info(warehouse)", result.contentObject.toString())
    }

    @Test fun placeOrderShouldSendARequestToAnOrderManager() {
        userAgent.placeOrder(
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

    @Test fun ordersShouldSendARequestToAnOrderManager() {
        userAgent.orders(user(client("x"), email("y"), address("z")))
        val result = orderManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""info(client("x"),email("y"))""", result.contentObject.toString())
    }
}
