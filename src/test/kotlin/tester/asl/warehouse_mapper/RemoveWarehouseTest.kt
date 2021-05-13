package tester.asl.warehouse_mapper

import framework.Framework.ASL
import framework.Framework.Utility.agent
import framework.Framework.waitingTime
import framework.Framework.test
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Item.remove
import controller.agent.communication.translation.out.AbstractionTerms.term
import controller.agent.communication.translation.out.OperationTerms.term
import framework.Framework.Utility.mid
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.lang.acl.ACLMessage.*
import org.junit.Test
import org.junit.Assert

/**
 * Test class for WarehouseMapper's remove item request.
 * Those tests assume a knowledge about the initial state of the warehouse.
 *
 * @author Paolo Baldini
 */
class RemoveWarehouseTest {
    private fun removeItem(item: Item.QuantityItem, mid : Int, annots: String = "") = "${remove(item).term()}" +
            "[${mid(mid)}" + (if (annots.isNotBlank()) "," else "") + "$annots]"

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun removeItemShouldFailIfItIsNotInTheWarehouse() = test {
        val item = item(id("Item 999"), quantity(1))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent < FAILURE + "error(${removeItem(item, 1)})"

        checkWarehouseState("""item(id("Item 999"))""", false)
    }

    @Test fun removeItemShouldFailIfGreaterRequestThanAvailable() = test {
        val item = item(id("Item 5"), quantity(999))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent < FAILURE + "error(${removeItem(item, 1)})"

        checkWarehouseState("""item(id("Item 999"))""", false)
    }

    @Test fun removeItemShouldDecreaseNumberOfItemInTheWarehouse() = test {
        val item = item(id("Item 5"), quantity(1))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent < CONFIRM + removeItem(item, 1, "position(rack(3),shelf(1),quantity(1))")

        checkWarehouseState("""item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(6)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    }

    @Test fun removedItemPositionShouldMatchWithTheDecrementPosition() = test {
        val item = item(id("Item 5"), quantity(1))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent < CONFIRM + removeItem(item, 1, "position(rack(3),shelf(1),quantity(1))")

        checkWarehouseState("""position(rack(3),shelf(1),quantity(6))""")
    }

    @Test fun forLargeRequestShouldBeAbleToDecrementQuantityInMultiplePositions() = test {
        val item = item(id("Item 5"), quantity(12))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent < CONFIRM + removeItem(item, 1,
            "position(rack(3),shelf(1),quantity(7)),position(rack(3),shelf(2),quantity(5))")

        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper
        val result = agent.blockingReceive(waitingTime)
        Assert.assertFalse(result.content.contains("""position(rack(3),shelf(1)"""))
        Assert.assertTrue(result.content.contains("""position(rack(3),shelf(2),quantity(4))]"""))
    }

    @Test fun itemShouldBeRemovedOnlyOnceIfTheMessageIsReceivedAgain() = test {
        val item = item(id("Item 5"), quantity(12))

        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent .. INFORM + removeItem(item, 1) > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        val expectedResponse = CONFIRM + removeItem(item, 1, "position(rack(3),shelf(1),quantity(7)),"
                + "position(rack(3),shelf(2),quantity(5))")

        agent < expectedResponse

        val result = agent.blockingReceive(waitingTime)
        Assert.assertFalse(result.content.contains("""position(rack(3),shelf(1)"""))
        Assert.assertTrue(result.content.contains("""position(rack(3),shelf(2),quantity(4))]"""))

        agent < expectedResponse
    }

    @Test fun removeItemsShouldFailIfOneIsNotInTheWarehouse() = test {
        val item1 = item(id("Item 5"), quantity(1))
        val item2 = item(id("Item 999"), quantity(1))

        agent .. REQUEST + "remove(items, oid)[mid(mid),${item1.term()},${item2.term()}]" > ASL.warehouseMapper
        agent < FAILURE + "error(remove(items,oid)[${item1.term()},${item2.term()},mid(mid)])"
        checkWarehouseState("""item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(7)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    }

    @Test fun removeItemsShouldFailIfOneIsRequiredInTooHighQuantity() = test {
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(999))

        agent .. REQUEST + "remove(items, oid)[mid(mid),${item1.term()},${item2.term()}]" > ASL.warehouseMapper
        agent < FAILURE + "error(remove(items,oid)[${item1.term()},${item2.term()},mid(mid)])"

        checkWarehouseState("""item(id("Item 2"))[position(rack(2),shelf(4),quantity(1))]""")
        checkWarehouseState("""item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(7)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    }

    @Test fun removedItemsPositionShouldMatchWithTheDecrementPosition() = test {
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(1))

        agent .. REQUEST + "remove(items,oid)[mid(mid),${item1.term()},${item2.term()}]" > ASL.warehouseMapper
        agent < CONFIRM + ("""remove(items,oid)[mid(mid),""" + """position(rack(2),shelf(4),quantity(1)),""" +
                """position(rack(3),shelf(1),quantity(1))]""")

        checkWarehouseState("""item(id("Item 2"))""", false)
        checkWarehouseState("""position(rack(3),shelf(1),quantity(6))""")
    }

    @Test fun forLargeItemsRequestShouldBeAbleToDecrementQuantityInMultiplePositions() = test {
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(12))

        agent .. REQUEST + "remove(items,oid)[${item1.term()},${item2.term()},mid(mid)]" > ASL.warehouseMapper
        agent < CONFIRM + ("""remove(items,oid)[mid(mid),""" + """position(rack(2),shelf(4),quantity(1)),""" +
                """position(rack(3),shelf(1),quantity(7)),""" + """position(rack(3),shelf(2),quantity(5))]""")

        checkWarehouseState("""item(id("Item 2"))""", false)
        checkWarehouseState("""position(rack(3),shelf(1)""", false)
        checkWarehouseState("""position(rack(3),shelf(2),quantity(4))]""")
    }

    private fun checkWarehouseState(string: String, contain: Boolean = true) {
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        val result = agent.blockingReceive(waitingTime)
        val test: (Boolean) -> Unit = if (contain) Assert::assertTrue else Assert::assertFalse
        test(result.content.contains(string))
    }
}
