package controller.agent.communication

import controller.agent.Agents.cyclicBehaviour
import framework.AMWSpecificFramework.Test.communicator
import framework.Framework.Utility.agent
import framework.Framework.test
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

    private val agentName = "agent-name"
    private val agentType = "agent-type"

    @Test fun sendMessageShouldEffectivelyDeliverIt() = test { agent.register(agentName, agentType)
        communicator.sendMessage(message())
        Assert.assertNotNull(agent.blockingReceive(receiveWaitingTime))
    }

    @Test fun sendMessageShouldAllowToReceiveAResponse() = test { agent.register(agentName, agentType)
        agent.addBehaviour(cyclicBehaviour {
            agent.send(agent.blockingReceive().createReply())
        })
        communicator.sendMessage(message())
            .get(receiveWaitingTime, TimeUnit.MILLISECONDS)                 // throws an exception if timeout elapse
    }

    @Test fun sendMessageShouldAllowToMarshallResponse() = test { agent.register(agentName, agentType)
        agent.addBehaviour(cyclicBehaviour {
            agent.send(agent.blockingReceive().createReply().apply { content = "text" })
        })
        val result = communicator.sendMessage(message()) {
            it.content                                                      // extract content from the message
        }.get(receiveWaitingTime, TimeUnit.MILLISECONDS)                    // throws an exception if timeout elapse
        Assert.assertEquals("text", result)
    }

    @Test fun sendMessageShouldTryOvercomeNetworkFailures() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        agent.addBehaviour(cyclicBehaviour {
            agent.blockingReceive()
            tryCounter.release()
        })
        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime + receiveWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertTrue(succeeded)
    }

    @Test fun sendMessageShouldNotRetryAfterSuccessfulDelivery() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        agent.addBehaviour(cyclicBehaviour {
            agent.send(agent.blockingReceive().createReply())
            tryCounter.release()
        })
        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    @Test fun sendMessageShouldNotRetryIfSpecified() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        agent.addBehaviour(cyclicBehaviour {
            agent.send(agent.blockingReceive().createReply())
            tryCounter.release()
        })
        communicator.sendMessage(message(), false) { }
        val succeeded = tryCounter.tryAcquire(2, retryWaitingTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)
    }

    private fun message() = ACLMessage().apply { addReceiver(agent.aid) }
}
