package tester.asl.collection_point_manager

import framework.AMWSpecificFramework.ASL
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging.compareTo
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
        agent .. INFORM + "free(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < FAILURE + "error(free(oid1)[mid(mid1)])"
    }

    @Test fun freeOfAnOccupiedPointShouldSucceed() = test {
        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(pid(0),x(50),y(50))[mid(mid1)]"

        agent .. INFORM + "free(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "free(oid1)[mid(mid1)]"
    }

    @Test fun afterFreeAPointShouldBeAgainAvailable() = test {
        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(pid(0),x(50),y(50))[mid(mid1)]"

        agent .. INFORM + "free(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "free(oid1)[mid(mid1)]"

        agent .. REQUEST + "point(oid2)[mid(mid2)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(pid(0),x(50),y(50))[mid(mid2)]"
    }
}
