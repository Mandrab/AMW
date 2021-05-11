package tester.asl.collection_point_manager

import framework.Framework.ASL
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.minus
import framework.Messaging.plus
import framework.Messaging.rangeTo
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert
import kotlin.random.Random

/**
 * Test class for CollectionPointManager's accept point reservation request
 *
 * @author Paolo Baldini
 */
class RequestPointTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun requestForPointShouldReturnIt() = test {
        agent .. INFORM + "point" > ASL.collectionPointManager
        agent < CONFIRM + """point(pid(0),x(50),y(50))"""
    }

    @Test fun twoRequestsShouldReturnDifferentPoints() = test {
        agent .. INFORM + "point" > ASL.collectionPointManager
        agent .. INFORM + "point" > ASL.collectionPointManager
        agent < CONFIRM + """point(pid(0),x(50),y(50))"""
        agent < CONFIRM + """point(pid(1),x(50),y(70))"""
    }

    @Test fun requestShouldFailIfEveryPointIsAlreadyReserved() = test {
        val requests = generateSequence {
            agent .. INFORM + "point" - Random.nextDouble().toString() > ASL.collectionPointManager
            agent.blockingReceive(waitingTime)
        }.take(7).toList()
        requests.take(6).forEach { Assert.assertEquals(CONFIRM, it.performative) }
        assert(requests.last(), FAILURE, "point")
    }

    @Test fun pointForAnAlreadySubmitterOrderShouldBeTheSame() = test {
        val orderId = "1234567890"
        agent .. INFORM + "point" - orderId > ASL.collectionPointManager
        agent < CONFIRM + """point(pid(0),x(50),y(50))""" - orderId

        agent .. INFORM + "point" - orderId > ASL.collectionPointManager
        agent < CONFIRM + """point(pid(0),x(50),y(50))""" - orderId
    }
}
