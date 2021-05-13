package controlled.agent.communication

import framework.Framework
import controller.agent.Agents.cyclicBehaviour
import framework.Framework.test
import framework.JADEAgent
import jade.lang.acl.ACLMessage
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Test class for communicator abstract agent
 *
 * @author Paolo Baldini
 */
class CommunicatorTest {
    private val receiveWaitingTime = 1000L
    private val retryWaitingTime = 5000L

    private val receiverName = "receiver-name"
    private val receiverType = "receiver-type"

    private val receiver = Framework.agent(JADEAgent::class.java).register(receiverName, receiverType)

    @Test fun sendMessageShouldEffectivelyDeliverIt() = test {
        agent(Communicator::class.java).sendMessage(message())
        Assert.assertNotNull(receiver.blockingReceive(receiveWaitingTime))
    }

    @Test fun sendMessageShouldAllowToReceiveAResponse() = test {
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
        })
        agent(Communicator::class.java).sendMessage(message())
            .get(receiveWaitingTime, TimeUnit.MILLISECONDS)                 // throws an exception if timeout elapse
    }

    @Test fun sendMessageShouldAllowToMarshallResponse() = test {
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply().apply { content = "text" })
        })
        val result = agent(Communicator::class.java).sendMessage(message()) {
            it.content                                                      // extract content from the message
        }.get(receiveWaitingTime, TimeUnit.MILLISECONDS)                    // throws an exception if timeout elapse
        Assert.assertEquals("text", result)
    }

    @Test fun sendMessageShouldTryOvercomeNetworkFailures() = test {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.blockingReceive()
            tryCounter.release()
        })
        agent(Communicator::class.java).sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime + receiveWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertTrue(succeeded)
    }

    @Test fun sendMessageShouldNotRetryAfterSuccessfulDelivery() = test {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
            tryCounter.release()
        })
        agent(Communicator::class.java).sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    @Test fun sendMessageShouldNotRetryIfSpecified() = test {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
            tryCounter.release()
        })
        agent(Communicator::class.java).sendMessage(message(), false) { }
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    private fun message() = ACLMessage().apply { addReceiver(receiver.aid) }
}
