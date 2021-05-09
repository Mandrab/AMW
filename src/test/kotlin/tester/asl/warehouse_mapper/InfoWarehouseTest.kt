package tester.asl.warehouse_mapper

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import jade.lang.acl.ACLMessage.INFORM
import org.junit.Test
import org.junit.Assert

/**
 * Test class for WarehouseMapper's info request
 *
 * @author Paolo Baldini
 */
class InfoWarehouseTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun infoRequestShouldReturnAList() = test { agent()() {
        sendRequest("info(warehouse)", agent("warehouse_mapper", ASLAgent::class.java).aid)
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(INFORM, result.performative)
        Assert.assertTrue(result.content.startsWith('[') && result.content.endsWith(']'))
    } }
}
