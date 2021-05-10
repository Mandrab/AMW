package tester.asl.collection_point_manager

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import jade.lang.acl.ACLMessage
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
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val result = agent().sendRequest("free", collectionPointManagerAID, INFORM).blockingReceive(waitingTime)

        assert(result, FAILURE, "free")
    }
}
