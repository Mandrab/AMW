package controlled.admin.agent

import controller.admin.agent.Agent as AdminAgent
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import framework.Framework.test
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Assert
import org.junit.Test

/**
 * Test class for admin agent
 * Some of the features are not tested here in that
 * they are (or will) be tested later in more adequate places
 *
 * @author Paolo Baldini
 */
class AgentTest {
    private val receiveWaitingTime = 1000L

    @Test fun addCommandShouldSendRequestToCommandManager() = test { commandManager
        agent(AdminAgent::class.java).addCommand()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun addItemShouldSendRequestToWarehouseMapper() = test { warehouseMapper
        agent(AdminAgent::class.java).addItem(item(id("a"), position(rack(2), shelf(3), quantity(2))))
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals(
            """add(item(id("a"),position(rack(2),shelf(3),quantity(2))))""",
            result.contentObject.toString()
        )
    }

    @Test fun removeItemShouldSendRequestToWarehouseMapper() = test { warehouseMapper
        agent(AdminAgent::class.java).removeItem(item(id("a"),quantity(3)))
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""remove(item(id("a"),quantity(3)))""", result.contentObject.toString())
    }

    @Test fun addVersionShouldSendRequestToCommandManager() = test { commandManager
        agent(AdminAgent::class.java).addVersion()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun executeCommandShouldSendRequestToSomeViableAgent() = test { commandManager
        agent(AdminAgent::class.java).executeCommand()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun executeScriptShouldSendRequestToSomeViableAgent() = test { commandManager
        agent(AdminAgent::class.java).executeScript()
        val result = commandManager.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("TODO", result.contentObject.toString())
    }

    @Test fun warehouseStateShouldSendRequestToWarehouseManager() = test { warehouseMapper
        agent(AdminAgent::class.java).warehouseState()
        val result = warehouseMapper.blockingReceive(receiveWaitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(REQUEST, result.performative)
        Assert.assertEquals("""info(warehouse)""", result.contentObject.toString())
    }
}
