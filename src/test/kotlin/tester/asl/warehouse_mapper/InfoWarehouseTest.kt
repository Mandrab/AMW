package tester.asl.warehouse_mapper

import framework.Framework.ASL
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.lang.acl.ACLMessage.INFORM
import jade.lang.acl.ACLMessage.REQUEST
import org.junit.Test
import org.junit.Assert

/**
 * Test class for WarehouseMapper's info request
 *
 * @author Paolo Baldini
 */
class InfoWarehouseTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun infoRequestShouldReturnAList() = test {
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        val result = agent.blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(INFORM, result.performative)
        Assert.assertTrue(result.content.startsWith('[') && result.content.endsWith(']'))
    }

    @Test fun infoRequestShouldReturnAListContainingItems() = test {
        agent .. REQUEST + "info(warehouse)" > ASL.warehouseMapper

        val result = agent.blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(INFORM, result.performative)
        Assert.assertTrue(result.content.contains("""item(id("Item 2"))[position(rack(2),shelf(4),quantity(1))]"""))
    }
}
