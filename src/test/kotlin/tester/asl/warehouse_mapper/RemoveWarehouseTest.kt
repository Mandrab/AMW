package tester.asl.warehouse_mapper

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Item.remove
import controller.agent.communication.translation.out.OperationTerms.term
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

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun removeItemShouldFailIfItIsNotInTheWarehouse() = test { agent()() {
        val item = item(id("Item 999"), quantity(1))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(remove(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, FAILURE, remove(item).term())
        Assert.assertFalse(result2.content.contains("""item(id("Item 999"))"""))
    } }

    @Test fun removeItemShouldFailIfGreaterRequestThanAvailable() = test { agent()() {
        val item = item(id("Item 5"), quantity(999))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(remove(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, FAILURE, remove(item).term())
        Assert.assertFalse(result2.content.contains("""item(id("Item 999"))"""))
    } }

    @Test fun removeItemShouldDecreaseNumberOfItemInTheWarehouse() = test { agent()() {
        val item = item(id("Item 5"), quantity(1))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(remove(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString() + """[position(rack(3),shelf(1),quantity(1))]""")
        Assert.assertTrue(result2.content.contains("""item(id("Item 5"))[""" +
                """position(rack(3),shelf(1),quantity(6)),""" +
                """position(rack(3),shelf(2),quantity(9))]""")
        )
    } }

    @Test fun removedItemPositionShouldMatchWithTheDecrementPosition() = test { agent()() {
        val item = item(id("Item 5"), quantity(1))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(remove(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString() + """[position(rack(3),shelf(1),quantity(1))]""")
        Assert.assertTrue(result2.content.contains("""position(rack(3),shelf(1),quantity(6))"""))
    } }

    @Test fun forLargeRequestShouldBeAbleToDecrementQuantityInMultiplePositions() = test { agent()() {
        val item = item(id("Item 5"), quantity(12))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(remove(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, remove(item).term().toString()
                + """[position(rack(3),shelf(1),quantity(7)),"""
                + """position(rack(3),shelf(2),quantity(5))]""")
        Assert.assertFalse(result2.content.contains("""position(rack(3),shelf(1)"""))
    } }
/*
    @Test fun addItemShouldSucceedIfSameItemIsAlreadyInThisPosition() = test { agent()() {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(add(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, add(item).term())
        Assert.assertTrue(
            result2.content.contains("""item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))""")
        )
    } }

    @Test fun addItemShouldFailIfADifferentItemIsAlreadyInThisPosition() = test { agent()() {
        val item = item(id("Item 999"), position(rack(3), shelf(1), quantity(3)))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        sendRequest(add(item).term(), warehouseMapperAID)
        val result1 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result2 = blockingReceive(waitingTime)

        assert(result1, FAILURE, add(item).term())
        Assert.assertTrue(
            result2.content.contains("""item(id("Item 5"))[position(rack(3),shelf(1),quantity(7))""")
        )
    } }

    @Test fun itemShouldBeAddedOnlyOnceIfTheMessageIsReceivedAgain() = test { agent()() {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid

        val message = ACLMessage(REQUEST).apply {
            addReceiver(warehouseMapperAID)
            content = add(item).term().toString()
            replyWith = Random.nextDouble().toString()
        }

        send(message)
        val result1 = blockingReceive(waitingTime)

        send(message)
        val result2 = blockingReceive(waitingTime)

        sendRequest("info(warehouse)", warehouseMapperAID)
        val result3 = blockingReceive(waitingTime)

        assert(result1, CONFIRM, add(item).term())
        Assert.assertEquals(result1.performative, result2.performative)
        Assert.assertEquals(result1.content, result2.content)
        Assert.assertEquals(result1.inReplyTo, result2.inReplyTo)
        Assert.assertTrue(
            result3.content.apply { println(this) }.contains("""item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))""")
        )
    } }*/
}
