package tester.asl.warehouse_mapper

import framework.AMWSpecificFramework.ASL
import framework.Framework.Utility.agent
import framework.Framework.test
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.operation.Item.add
import controller.agent.communication.translation.out.OperationTerms.term
import framework.AMWSpecificFramework.mid
import framework.AMWSpecificFramework.waitingTime
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.lang.acl.ACLMessage.*
import org.junit.Test
import org.junit.Assert

/**
 * Test class for WarehouseMapper's add item request.
 * Those tests assume a knowledge about the initial state of the warehouse.
 *
 * @author Paolo Baldini
 */
class AddWarehouseTest {
    private fun addItem(item: Item.WarehouseItem, mid : Int) = "${add(item).term()}[${mid(mid)}]"

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun addItemShouldSucceedIfThePositionIsFree() = test {
        val item = item(id("Item 999"), position(rack(999), shelf(999), quantity(999)))

        agent .. REQUEST + addItem(item, 1) > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < CONFIRM + addItem(item, 1)
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 999"))[position(rack(999),shelf(999),quantity(999))]"""
        ))
    }

    @Test fun addItemShouldSucceedIfSameItemIsAlreadyInThisPosition() = test {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + addItem(item, 1) > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < CONFIRM + addItem(item, 1)
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))"""
        ))
    }

    @Test fun addItemShouldFailIfADifferentItemIsAlreadyInThisPosition() = test {
        val item = item(id("Item 999"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + addItem(item, 1) > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < FAILURE + "error(${addItem(item, 1)})"
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 5"))[position(rack(3),shelf(1),quantity(7))"""
        ))
    }

    @Test fun itemShouldBeAddedOnlyOnceIfTheMessageIsReceivedAgain() = test {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + addItem(item, 1) > ASL.warehouseMapper
        agent .. REQUEST + addItem(item, 1) > ASL.warehouseMapper

        agent <= CONFIRM + addItem(item, 1)
        agent <= CONFIRM + addItem(item, 1)

        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue("""Expected contains: item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))""" +
                "\nBut was: ${result.content}",
            result.content.contains("""item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))"""
        ))
    }
}
