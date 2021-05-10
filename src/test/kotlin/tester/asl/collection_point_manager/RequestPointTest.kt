package tester.asl.collection_point_manager

import common.ASLAgent
import common.Framework
import common.Framework.Companion.waitingTime
import common.Framework.Companion.retryTime
import common.Framework.Companion.test
import common.JADEAgent
import common.ontology.Services.ServiceType.*
import common.ontology.Services.ServiceSupplier.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.Agents.oneShotBehaviour
import controller.agent.communication.translation.out.OperationTerms.term
import jade.core.AID
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Test class for CollectionPointManager's accept point reservation request
 *
 * @author Paolo Baldini
 */
class RequestPointTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun requestForPointShouldReturnIt() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val result = agent().sendRequest(
            "point",
            collectionPointManagerAID,
            INFORM
        ).blockingReceive(waitingTime)
        assert(result, CONFIRM, """point(pid(0),x(50),y(50))""")
    }

    @Test fun twoRequestsShouldReturnDifferentPoints() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val result1 = agent().sendRequest(
            "point",
            collectionPointManagerAID,
            INFORM
        ).blockingReceive(waitingTime)
        val result2 = agent().sendRequest(
            "point",
            collectionPointManagerAID,
            INFORM
        ).blockingReceive(waitingTime)
        assert(result1, CONFIRM, """point(pid(0),x(50),y(50))""")
        assert(result2, CONFIRM, """point(pid(1),x(50),y(70))""")
    }

    @Test fun requestShouldFailIfEveryPointIsAlreadyReserved() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val client = agent()

        val requests = generateSequence {
            client.sendRequest(
                "point",
                collectionPointManagerAID,
                INFORM
            ).blockingReceive(waitingTime)
        }.take(7).toList()
        requests.take(6).forEach { Assert.assertEquals(CONFIRM, it.performative) }
        assert(requests.last(), FAILURE, "point")
    }
}
