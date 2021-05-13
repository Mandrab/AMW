package tester.asl

import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import framework.AMWSpecificFramework.ASL.collectionPointManager
import framework.AMWSpecificFramework.ASL.orderManager
import framework.AMWSpecificFramework.ASL.robotPicker
import framework.AMWSpecificFramework.ASL.warehouseMapper
import framework.AMWSpecificFramework.waitingTime
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging
import framework.Messaging.compareTo
import framework.Messaging.minus
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.core.Agent
import jade.lang.acl.ACLMessage.*
import org.junit.Assert
import org.junit.Test

class MessagePassingTest {
    private val defaultOrder = order(client("x"), email("y"), address("z"))[
            item(ID.id("Item 1"), quantity(3)),
            item(ID.id("Item 2"), quantity(1))
    ].term()

    @Test fun orderPropagation() = test(filterLogs = true) {
        robotPicker; collectionPointManager; warehouseMapper; orderManager

        recordLogs = true
        Thread.sleep(250)

        agent .. REQUEST + defaultOrder - "abc" > orderManager      ; -"[ORDER MANAGER] request for a new order"
        agent <= CONFIRM + defaultOrder

        // expected logs
        -"[WAREHOUSE MAPPER] required items removal"
        -"[ORDER MANAGER] order confirmed by warehouse"
        -"[COLLECTION POINT MANAGER] collection point request"
        -"[ORDER MANAGER] collection point confirmed"
        -"[ROBOT PICKER] request for item retrieval"
        -"[ROBOT PICKER] request for item retrieval"
        -"[ROBOT PICKER] failed request"
        -"[ORDER MANAGER] item retrieved"
        -"[ROBOT PICKER] request for item retrieval"
        -"[ORDER MANAGER] last item retrieved"
        -"[COLLECTION POINT MANAGER] collection point free"

        Thread.sleep(5000)
        recordLogs = false

        agent .. REQUEST + "info(warehouse)" > warehouseMapper
        agent <= INFORM + "position(rack(1),shelf(1),quantity(2))"
    }

    private operator fun Agent.compareTo(message: Messaging.Message) = 0.apply {
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(message.performative, result.performative)
        Assert.assertTrue(result.content.contains(message.content))
    }
}
