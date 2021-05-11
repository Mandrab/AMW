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

/**
 * Test class for CollectionPointManager's free point reservation
 *
 * @author Paolo Baldini
 */
class FreePointTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun freeOfANonOccupiedPointShouldFail() = test {
        agent .. INFORM + "free" > ASL.collectionPointManager
        agent < FAILURE + "free"
    }

    @Test fun freeOfAnOccupiedPointShouldSucceed() = test {
        val client1 = agent()
        client1 .. INFORM + "point" - "1234567890" > ASL.collectionPointManager
        client1 < CONFIRM + """point(pid(0),x(50),y(50))""" - "1234567890"

        client1 .. INFORM + "free" - "1234567890" > ASL.collectionPointManager
        client1 < CONFIRM + "free" - "1234567890"
    }

    @Test fun afterFreeAPointShouldBeAgainAvailable() = test {
        val client1 = agent()
        client1 .. INFORM + "point" - "1234567890" > ASL.collectionPointManager
        client1 < CONFIRM + """point(pid(0),x(50),y(50))""" - "1234567890"

        client1 .. INFORM + "free" - "1234567890" > ASL.collectionPointManager
        client1 < CONFIRM + "free" - "1234567890"

        client1 .. INFORM + "point" - "1234567890" > ASL.collectionPointManager
        client1 < CONFIRM + """point(pid(0),x(50),y(50))""" - "1234567890"
    }
}
