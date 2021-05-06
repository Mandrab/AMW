package controlled.agent.communication

import common.JADEAgents.proxy
import common.JADEAgents.register
import controller.agent.Agents.cyclicBehaviour
import jade.lang.acl.ACLMessage
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Test class for communicator abstract agent
 *
 * @author Paolo Baldini
 */
class CommunicatorTest {
    private val receiveWaitingTime = 1000L
    private val retryWaitingTime = 5000L

    private val communicatorName = "communicator-name" + Random.nextDouble()
    private val receiverName = "receiver-name" + Random.nextDouble()
    private val receiverType = "receiver-type"

    private val communicator = proxy(communicatorName, Communicator().javaClass.canonicalName).agent as Communicator
    private val receiver = proxy(receiverName).agent.apply { register(receiverName, receiverType) }

    @Test fun sendMessageShouldEffectivelyDeliverIt() {
        communicator.sendMessage(message())
        Assert.assertNotNull(receiver.blockingReceive(receiveWaitingTime))
    }

    @Test fun sendMessageShouldAllowToReceiveAResponse() {
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
        })
        communicator.sendMessage(message())
            .get(receiveWaitingTime, TimeUnit.MILLISECONDS)                 // throws an exception if timeout elapse
    }

    @Test fun sendMessageShouldAllowToMarshallResponse() {
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply().apply { content = "text" })
        })
        val result = communicator.sendMessage(message()) {
            it.content                                                      // extract content from the message
        }.get(receiveWaitingTime, TimeUnit.MILLISECONDS)                    // throws an exception if timeout elapse
        Assert.assertEquals("text", result)
    }

    @Test fun sendMessageShouldTryOvercomeNetworkFailures() {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.blockingReceive()
            tryCounter.release()
        })
        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime + receiveWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertTrue(succeeded)
    }

    @Test fun sendMessageShouldNotRetryAfterSuccessfulDelivery() {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
            tryCounter.release()
        })
        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    @Test fun sendMessageShouldNotRetryIfSpecified() {
        val tryCounter = Semaphore(0)
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
            tryCounter.release()
        })
        communicator.sendMessage(message(), false) { }
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    private fun message() = ACLMessage().apply { addReceiver(receiver.aid) }
}
