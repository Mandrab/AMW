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
 * Test class for WarehouseMapper's add item request
 *
 * @author Paolo Baldini
 */
class AddWarehouseTest {
    private val item = add(item(id("Item 999"), position(rack(999), shelf(999), quantity(999)))).term()

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun addItemShouldSucceedIfThePositionIsFree() = test { agent()() {
        val warehouseMapperAID = agent("warehouse_mapper", ASLAgent::class.java).aid
        sendRequest(item, warehouseMapperAID)

        val result = blockingReceive(waitingTime)
        assert(result, CONFIRM, item)
    } }

    //@Test fun addItemShouldSucceedIfSameItemIsAlreadyInThisPosition()

    //@Test fun addItemShouldFailIfADifferentItemIsAlreadyInThisPosition()

    //@Test fun itemShouldBeAddedOnlyOnceIfTheMessageIsRecivedAgain()
}
