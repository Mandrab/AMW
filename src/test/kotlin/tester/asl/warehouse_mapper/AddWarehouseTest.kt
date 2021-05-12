package tester.asl.warehouse_mapper

import framework.Framework.ASL
import framework.Framework.Utility.agent
import framework.Framework.test
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.operation.Item.add
import controller.agent.communication.translation.out.OperationTerms.term
import framework.Messaging.compareTo
import framework.Messaging.minus
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

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun addItemShouldSucceedIfThePositionIsFree() = test {
        val item = item(id("Item 999"), position(rack(999), shelf(999), quantity(999)))

        agent .. REQUEST + add(item).term() > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < CONFIRM + add(item).term()
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 999"))[position(rack(999),shelf(999),quantity(999))]"""
        ))
    }

    @Test fun addItemShouldSucceedIfSameItemIsAlreadyInThisPosition() = test { agent()() {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + add(item).term() > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < CONFIRM + add(item).term()
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))"""
        ))
    } }

    @Test fun addItemShouldFailIfADifferentItemIsAlreadyInThisPosition() = test {
        val item = item(id("Item 999"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + add(item).term() > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        agent < FAILURE + "error(${add(item).term().toString().trim()})"
        val result = agent.blockingReceive(waitingTime)
        Assert.assertTrue(result.content.contains(
            """item(id("Item 5"))[position(rack(3),shelf(1),quantity(7))"""
        ))
    }

    @Test fun itemShouldBeAddedOnlyOnceIfTheMessageIsReceivedAgain() = test {
        val item = item(id("Item 5"), position(rack(3), shelf(1), quantity(3)))

        agent .. REQUEST + add(item).term() - "abc" > ASL.warehouseMapper
        agent .. REQUEST + add(item).term() - "abc" > ASL.warehouseMapper
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        val result1 = agent.blockingReceive(waitingTime)
        val result2 = agent.blockingReceive(waitingTime)
        val result3 = agent.blockingReceive(waitingTime)

        assert(result1, CONFIRM, add(item).term())
        Assert.assertEquals(result1.performative, result3.performative)
        Assert.assertEquals(result1.content, result3.content)
        Assert.assertEquals(result1.inReplyTo, result3.inReplyTo)
        Assert.assertTrue(result2.content.contains(
            """item(id("Item 5"))[position(rack(3),shelf(1),quantity(10))"""
        ))
    }
}
