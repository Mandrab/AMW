package tester.asl.warehouse_mapper

import framework.ASLAgent
import framework.Framework.waitingTime
import framework.Framework.test
import framework.JADEAgent
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Item.remove
import controller.agent.communication.translation.out.AbstractionTerms.term
import controller.agent.communication.translation.out.OperationTerms.term
import jade.core.AID
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.*
import org.junit.Test
import org.junit.Assert
import kotlin.random.Random

/**
 * Test class for WarehouseMapper's remove item request.
 * Those tests assume a knowledge about the initial state of the warehouse.
 *
 * @author Paolo Baldini
 */
class RemoveWarehouseTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun removeItemShouldFailIfItIsNotInTheWarehouse() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 999"), quantity(1))

        sendRequest(remove(item).term(), warehouseMapperAID, INFORM)
        val result1 = blockingReceive(waitingTime)
        assert(result1, FAILURE, remove(item).term())

        checkWarehouseState(warehouseMapperAID, """item(id("Item 999"))""", false)
    } }

    @Test fun removeItemShouldFailIfGreaterRequestThanAvailable() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 5"), quantity(999))

        sendRequest(remove(item).term(), warehouseMapperAID, INFORM)
        val result1 = blockingReceive(waitingTime)
        assert(result1, FAILURE, remove(item).term())

        checkWarehouseState(warehouseMapperAID, """item(id("Item 999"))""", false)
    } }

    @Test fun removeItemShouldDecreaseNumberOfItemInTheWarehouse() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 5"), quantity(1))

        sendRequest(remove(item).term(), warehouseMapperAID, INFORM)
        val result1 = blockingReceive(waitingTime)
        assert(result1, CONFIRM, remove(item).term().toString() + """[position(rack(3),shelf(1),quantity(1))]""")

        checkWarehouseState(warehouseMapperAID, """item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(6)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    } }

    @Test fun removedItemPositionShouldMatchWithTheDecrementPosition() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 5"), quantity(1))

        sendRequest(remove(item).term(), warehouseMapperAID, INFORM)
        val result1 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString() + """[position(rack(3),shelf(1),quantity(1))]""")

        checkWarehouseState(warehouseMapperAID, """position(rack(3),shelf(1),quantity(6))""")
    } }

    @Test fun forLargeRequestShouldBeAbleToDecrementQuantityInMultiplePositions() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 5"), quantity(12))

        sendRequest(remove(item).term(), warehouseMapperAID, INFORM)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString()
                + """[position(rack(3),shelf(1),quantity(7)),"""
                + """position(rack(3),shelf(2),quantity(5))]""")
        Assert.assertFalse(result2.content.contains("""position(rack(3),shelf(1)"""))
        Assert.assertTrue(result2.content.contains("""position(rack(3),shelf(2),quantity(4))]"""))
    } }

    @Test fun itemShouldBeRemovedOnlyOnceIfTheMessageIsReceivedAgain() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item = item(id("Item 5"), quantity(12))

        val message = ACLMessage(INFORM).apply {
            addReceiver(warehouseMapperAID)
            content = remove(item).term().toString()
            replyWith = Random.nextDouble().toString()
        }

        send(message)
        val result1 = blockingReceive(waitingTime)

        send(message)
        val result2 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result3 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString()
                + """[position(rack(3),shelf(1),quantity(7)),"""
                + """position(rack(3),shelf(2),quantity(5))]""")
        Assert.assertEquals(result1.performative, result2.performative)
        Assert.assertEquals(result1.content, result2.content)
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
        Assert.assertFalse(result3.content.contains("""position(rack(3),shelf(1)"""))
        Assert.assertTrue(result3.content.contains("""position(rack(3),shelf(2),quantity(4))]"""))
    } }

    @Test fun removeItemsShouldFailIfOneIsNotInTheWarehouse() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item1 = item(id("Item 5"), quantity(1))
        val item2 = item(id("Item 999"), quantity(1))

        sendRequest("remove(items)[${item1.term()},${item2.term()}]", warehouseMapperAID, INFORM)
        val result = blockingReceive(waitingTime)

        assert(result, FAILURE, "remove(items)[${item1.term()},${item2.term()}]")
        checkWarehouseState(warehouseMapperAID, """item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(7)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    } }

    @Test fun removeItemsShouldFailIfOneIsRequiredInTooHighQuantity() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(999))

        sendRequest("remove(items)[${item1.term()},${item2.term()}]", warehouseMapperAID, INFORM)
        val result = blockingReceive(waitingTime)

        assert(result, FAILURE, "remove(items)[${item1.term()},${item2.term()}]")
        checkWarehouseState(warehouseMapperAID, """item(id("Item 2"))[position(rack(2),shelf(4),quantity(1))]""")
        checkWarehouseState(warehouseMapperAID, """item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(7)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
    } }

    @Test fun removedItemsPositionShouldMatchWithTheDecrementPosition() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(1))

        sendRequest("remove(items)[${item1.term()},${item2.term()}]", warehouseMapperAID, INFORM)
        val result = blockingReceive(waitingTime)

        assert(result, CONFIRM, """remove(items)[""" +
                """position(rack(2),shelf(4),quantity(1)),""" +
                """position(rack(3),shelf(1),quantity(1))]""")

        checkWarehouseState(warehouseMapperAID, """item(id("Item 2"))""", false)
        checkWarehouseState(warehouseMapperAID, """position(rack(3),shelf(1),quantity(6))""")
    } }

    @Test fun forLargeItemsRequestShouldBeAbleToDecrementQuantityInMultiplePositions() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        val item1 = item(id("Item 2"), quantity(1))
        val item2 = item(id("Item 5"), quantity(12))

        sendRequest("remove(items)[${item1.term()},${item2.term()}]", warehouseMapperAID, INFORM)
        val result = blockingReceive(waitingTime)
        assert(result, CONFIRM, """remove(items)[""" +
                """position(rack(2),shelf(4),quantity(1)),""" +
                """position(rack(3),shelf(1),quantity(7)),""" +
                """position(rack(3),shelf(2),quantity(5))]""")

        checkWarehouseState(warehouseMapperAID, """item(id("Item 2"))""", false)
        checkWarehouseState(warehouseMapperAID, """position(rack(3),shelf(1)""", false)
        checkWarehouseState(warehouseMapperAID, """position(rack(3),shelf(2),quantity(4))]""")
    } }

    private fun JADEAgent.checkWarehouseState(warehouseMapperAID: AID, string: String, contain: Boolean = true) {
        sendRequest("info(warehouse)", warehouseMapperAID)
        val result = blockingReceive(waitingTime)
        val test: (Boolean) -> Unit = if (contain) Assert::assertTrue else Assert::assertFalse
        test(result.content.contains(string))
    }
}
