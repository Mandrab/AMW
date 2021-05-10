package tester.asl.collection_point_manager

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import jade.lang.acl.ACLMessage
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

    @Test fun pointForAnAlreadySubmitterOrderShouldBeTheSame() = test {
        val collectionPointManagerAID = agent("collection_point_manager", ASLAgent::class.java).aid
        val message = ACLMessage(INFORM).apply {
            addReceiver(collectionPointManagerAID)
            content = "point"
            replyWith = "1234567890"
        }

        val client1 = agent()
        client1.send(message.apply { sender = client1.aid })
        val result1 = client1.blockingReceive(waitingTime)
        val client2 = agent()
        client2.send(message.apply { sender = client2.aid })
        val result2 = client2.blockingReceive(waitingTime)

        Assert.assertEquals(result1.performative, result2.performative)
        Assert.assertEquals(result1.content, result2.content)
    }
}
