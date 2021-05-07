package controlled.admin.agent

import common.Framework
import controller.admin.agent.Agent as AdminAgent
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

/**
 * Test class for admin agent
 * Some of the features are not tested here in that
 * they are (or will) be tested later in more adequate places
 *
 * @author Paolo Baldini
 */
class AgentTest: Framework() {
    private val receiveWaitingTime = 1000L

    private val adminAgent = agent(AdminAgent::class.java)
    private val commandManager = agent().register(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id)
    private val warehouseMapper = agent().register(MANAGEMENT_ITEMS.id, STORE_ITEM.id,REMOVE_ITEM.id,INFO_WAREHOUSE.id)

    @Test fun addCommandShouldSendRequestToCommandManager() {
        adminAgent.addCommand()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun addItemShouldSendRequestToWarehouseMapper() {
        adminAgent.addItem(item(id("a"), position(rack(2), shelf(3), quantity(2))))
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals(
            """add(item(id("a"),position(rack(2),shelf(3),quantity(2))))""",
            result.contentObject.toString()
        )
    }

    @Test fun removeItemShouldSendRequestToWarehouseMapper() {
        adminAgent.removeItem(item(id("a"),quantity(3)))
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""remove(item(id("a"),quantity(3)))""", result.contentObject.toString())
    }

    @Test fun addVersionShouldSendRequestToCommandManager() {
        adminAgent.addVersion()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun executeCommandShouldSendRequestToSomeViableAgent() {
        adminAgent.executeCommand()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun executeScriptShouldSendRequestToSomeViableAgent() {
        adminAgent.executeScript()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun warehouseStateShouldSendRequestToWarehouseManager() {
        adminAgent.warehouseState()
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""info(warehouse)""", result.contentObject.toString())
    }
}
