package tester.asl.collection_point_manager

import framework.AMWSpecificFramework.ASL
import framework.AMWSpecificFramework.waitingTime
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for CollectionPointManager's accept point reservation request
 *
 * @author Paolo Baldini
 */
class RequestPointTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun requestForPointShouldReturnIt() = test {
        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(oid1,pid(0),x(50),y(50))[mid(mid1)]"
    }

    @Test fun twoRequestsShouldReturnDifferentPoints() = test {
        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent .. REQUEST + "point(oid2)[mid(mid2)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(oid1,pid(0),x(50),y(50))[mid(mid1)]"
        agent < CONFIRM + "point(oid2,pid(1),x(50),y(70))[mid(mid2)]"
    }

    @Test fun requestShouldFailIfEveryPointIsAlreadyReserved() = test { var i = 0
        generateSequence { agent .. REQUEST + "point($i)[mid(${i++})]" > ASL.collectionPointManager }
            .take(6).toList()
            .map { agent.blockingReceive(waitingTime) }
            .forEach { Assert.assertEquals(CONFIRM, it.performative) }
        agent .. REQUEST + "point($i)[mid(${i++})]" > ASL.collectionPointManager
        agent <= FAILURE + "error(point(6)[mid(6)])"
    }

    @Test fun pointForAnAlreadySubmitterOrderShouldBeTheSame() = test {
        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(oid1,pid(0),x(50),y(50))[mid(mid1)]"

        agent .. REQUEST + "point(oid1)[mid(mid1)]" > ASL.collectionPointManager
        agent < CONFIRM + "point(oid1,pid(0),x(50),y(50))[mid(mid1)]"
    }
}
