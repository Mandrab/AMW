package controller.admin.agent

import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf.shelf
import framework.AMWSpecificFramework.ASL
import framework.AMWSpecificFramework.Test.admin
import framework.Framework.test
import jade.domain.FIPAException
import org.junit.Assert
import org.junit.Test

/**
 * Test class for admin agent requests return
 * Some of the features are not tested here in that
 * they are (or will) be tested later in more adequate places
 *
 * @author Paolo Baldini
 */
class AgentInTest {
    private val registrationTime = 500L

    @Test fun commandListShouldCompleteFutureWithExistingCommand() = test { ASL.commandManager
        Thread.sleep(registrationTime)

        val commands = ensure { admin.commandsList() }.get()
        Assert.assertArrayEquals(arrayOf(
                command(id("Command1"),name("command 1 name"),description("description command 1"))[
                        script("[{@l1 +!main <- .println('Executing script ...');.wait(500);!b}, " +
                                "{@l2 +!b <- .println('Script executed')}]")
                ]
            ),
            commands.toTypedArray()
        )
    }

    @Test fun warehouseStateShouldSendRequestToWarehouseManager() = test { ASL.warehouseMapper
        Thread.sleep(registrationTime)

        val items = ensure { admin.warehouseState() }.get()
        Assert.assertArrayEquals(arrayOf(
                item(id("Item 1"))[
                        position(rack(1),shelf(1),quantity(5)),
                        position(rack(1),shelf(2),quantity(8)),
                        position(rack(2),shelf(3),quantity(7))
                ],
                item(id("Item 2"))[position(rack(2),shelf(4),quantity(1))],
                item(id("Item 3"))[position(rack(2),shelf(5),quantity(1))],
                item(id("Item 4"))[position(rack(2),shelf(6),quantity(3))],
                item(id("Item 5"))[
                        position(rack(3),shelf(1),quantity(7)),
                        position(rack(3),shelf(2),quantity(9))
                ],
                item(id("Item 6"))[
                        position(rack(3),shelf(3),quantity(17)),
                        position(rack(3),shelf(4),quantity(19))
                ]
            ),
            items.toTypedArray()
        )
    }

    private tailrec fun <T> ensure(function: () -> T): T {
        try {
            return function()
        } catch (e: FIPAException) {
            if (! e.message!!.contains("Timeout searching for data into df")) throw e
        }
        return ensure(function)
    }
}
