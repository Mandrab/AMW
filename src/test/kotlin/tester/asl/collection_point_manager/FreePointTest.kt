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

    @Test fun freeOfAnOccupiedPointShouldSucceed() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val message = ACLMessage(INFORM).apply {
            addReceiver(collectionPointManagerAID)
            replyWith = "1234567890"
        }

        val client1 = agent()
        message.apply {
            content = "point"
            sender = client1.aid
        }
        val result1 = client1.apply { send(message) }.blockingReceive(waitingTime)
        val client2 = agent()
        message.apply {
            content = "free"
            sender = client2.aid
        }
        val result2 = client2.apply { send(message) }.blockingReceive(waitingTime)

        assert(result1, CONFIRM, """point(pid(0),x(50),y(50))""")
        assert(result2, CONFIRM, "free")
        Assert.assertEquals("1234567890", result2.inReplyTo)
    }

    @Test fun afterFreeAPointShouldBeAgainAvailable() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val message = ACLMessage(INFORM).apply {
            addReceiver(collectionPointManagerAID)
            replyWith = "1234567890"
        }

        val client1 = agent()
        message.apply {
            content = "point"
            sender = client1.aid
        }
        val result1 = client1.apply { send(message) }.blockingReceive(waitingTime)
        val client2 = agent()
        message.apply {
            content = "free"
            sender = client2.aid
        }
        client2.apply { send(message) }.blockingReceive(waitingTime)

        message.apply {
            content = "point"
            sender = client1.aid
        }
        val result2 = client1.sendRequest("point", collectionPointManagerAID, INFORM)
            .blockingReceive(waitingTime)

        assert(result2, CONFIRM, """point(pid(0),x(50),y(50))""")
    }
}
