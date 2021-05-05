package controlled.agent.communication

import common.SupportAgent
import common.TestAgents.find
import common.TestAgents.proxy
import common.TestAgents.register
import controller.agent.Agents.cyclicBehaviour
import jade.core.AID
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CommunicatorTest {
    private val receiveWaitingTime = 1000L
    private val retryWaitingTime = 5000L

    private val communicatorName = "communicator-name" + Random.nextDouble()
    private val receiverName = "receiver-name" + Random.nextDouble()
    private val receiverType = "receiver-type"

    private lateinit var receiverAID: AID
    private lateinit var communicator: SupportAgent
    private lateinit var receiver: Agent

    @Before fun setup() {
        communicator = proxy(communicatorName, SupportAgent().javaClass.canonicalName).agent as SupportAgent
        receiver = proxy(receiverName).agent
        receiver.register(receiverName, receiverType)
        receiverAID = communicator.find(receiverName, receiverType)
    }

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
        Assert.assertTrue(result == "text")
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

    @Test fun sendMessageShouldNotRetryAfterSuccesfulDelivery() {
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

    private fun message() = ACLMessage().apply { addReceiver(receiverAID) }
}
