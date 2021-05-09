package tester.asl.warehouse_mapper

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.operation.Item.add
import controller.agent.communication.translation.out.OperationTerms.term
import jade.lang.acl.ACLMessage.CONFIRM
import org.junit.Test
import org.junit.Assert

/**
 * Test class for WarehouseMapper's add item request.
 * Those tests assume a knowledge about the initial state of the warehouse.
 *
 * @author Paolo Baldini
 */
class AddWarehouseTest {
    private val newItem = item(id("Item 999"), position(rack(999), shelf(999), quantity(999)))
    private val item = item(id("Item 5"))[
            position(rack(3), shelf(1), quantity(7)),
            position(rack(3), shelf(2), quantity(9))
    ]

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun addItemShouldSucceedIfThePositionIsFree() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(add(newItem).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, add(newItem).term())
        Assert.assertTrue(
            result2.content.contains("""item(id("Item 999"))[position(rack(999),shelf(999),quantity(999))]""")
        )
    } }

    @Test fun addItemShouldSucceedIfSameItemIsAlreadyInThisPosition() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(
            add(item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))).term(),
            warehouseMapperAID
        )
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, add(item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))).term())
        Assert.assertTrue(
            result2.content.contains("""item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))""")
        )
    } }

    //@Test fun addItemShouldFailIfADifferentItemIsAlreadyInThisPosition()

    //@Test fun itemShouldBeAddedOnlyOnceIfTheMessageIsRecivedAgain()
}
