package controlled.user.agent

import controller.user.agent.Agent as UserAgent
import controller.user.agent.Proxy
import common.TestAgents.proxy
import common.TestAgents.register
import common.ontology.Services
import controller.agent.Agents
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class AgentTest {
    private val receiveWaitingTime = 1000L

    private val userName = "user-name" + Random.nextDouble()
    private val receiverName = Services.ServiceSupplier.MANAGEMENT_ITEMS.id
    private val receiverType = Services.ServiceType.INFO_WAREHOUSE.id

    private val userAgent = Proxy().apply { Agents.start(false)(listOf(this).toTypedArray())(UserAgent::class.java) }
    private val warehouseMapper = proxy(userName).agent.apply { register(receiverName, receiverType) }

    @Test fun shopItemShouldRequireAListOfItemsFromItemManager() {
        userAgent.shopItems()
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("info(warehouse)", result.contentObject.toString())
    }

    @Test fun shopItemShouldSendARequestToAnOrderManager() {

    }
}
