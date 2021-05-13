package controlled.admin.agent

import controller.admin.agent.Agent as AdminAgent
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import framework.AMWSpecificFramework.JADE
import framework.AMWSpecificFramework.Test.admin
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.plus
import jade.lang.acl.ACLMessage.REQUEST
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

    @Test fun addCommandShouldSendRequestToCommandManager() = test { JADE.commandManager
        admin.addCommand()

        JADE.commandManager < REQUEST + "TODO"
    }

    @Test fun addItemShouldSendRequestToWarehouseMapper() = test { JADE.warehouseMapper
        admin.addItem(item(id("a"), position(rack(2), shelf(3), quantity(2))))

        JADE.warehouseMapper < REQUEST + """add(item(id("a"),position(rack(2),shelf(3),quantity(2))))"""
    }

    @Test fun removeItemShouldSendRequestToWarehouseMapper() = test { JADE.warehouseMapper
        admin.removeItem(item(id("a"),quantity(3)))

        JADE.warehouseMapper < REQUEST + """remove(item(id("a"),quantity(3)))"""
    }

    @Test fun addVersionShouldSendRequestToCommandManager() = test { JADE.commandManager
        admin.addVersion()

        JADE.commandManager < REQUEST + "TODO"
    }

    @Test fun executeCommandShouldSendRequestToSomeViableAgent() = test { JADE.commandManager
        admin.executeCommand()

        JADE.commandManager < REQUEST + "TODO"
    }

    @Test fun executeScriptShouldSendRequestToSomeViableAgent() = test { JADE.commandManager
        admin.executeScript()

        JADE.commandManager < REQUEST + "TODO"
    }

    @Test fun warehouseStateShouldSendRequestToWarehouseManager() = test { JADE.warehouseMapper
        admin.warehouseState()

        JADE.warehouseMapper < REQUEST + """info(warehouse)"""
    }
}
