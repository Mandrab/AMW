package controller.agent.communication

import controller.agent.Agents.oneShotBehaviour
import controller.agent.Agents.cyclicBehaviour
import framework.AMWSpecificFramework.Test.communicator
import framework.AMWSpecificFramework.waitingTime
import framework.Framework.Utility.agent
import framework.Framework.test
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.REQUEST
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
    private val retryTime = 5000L

    private val agentName = "agent-name"
    private val agentType = "agent-type"

    @Test fun sendMessageShouldEffectivelyDeliverIt() = test { agent.register(agentName, agentType)
        communicator.sendMessage(message())
        Assert.assertNotNull(agent.blockingReceive(waitingTime))
    }

    @Test fun sendMessageShouldAllowToReceiveAResponse() = test { agent.register(agentName, agentType)
        agent.addBehaviour(oneShotBehaviour {
            agent.send(agent.blockingReceive().createReply())
        })
        communicator.sendMessage(message())
            .get(waitingTime, TimeUnit.MILLISECONDS)                 // throws an exception if timeout elapse
    }

    @Test fun sendMessageShouldAllowToMarshallResponse() = test { agent.register(agentName, agentType)
        agent.addBehaviour(oneShotBehaviour {
            agent.send(agent.blockingReceive().createReply().apply { content = "text" })
        })
        val result = communicator.sendMessage(message()) {
            it.content                                                      // extract content from the message
        }.get(waitingTime, TimeUnit.MILLISECONDS)                           // throws an exception if timeout elapse
        Assert.assertEquals("text", result)
    }

    @Test fun sendMessageShouldTryOvercomeNetworkFailures() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        val behaviour = receiveAndCount(tryCounter)
        agent.addBehaviour(behaviour)

        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryTime + waitingTime, TimeUnit.MILLISECONDS)
        Assert.assertTrue(succeeded)

        agent.removeBehaviour(behaviour)
    }

    @Test fun sendMessageShouldNotRetryAfterSuccessfulDelivery() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        val behaviour = receiveAndCount(tryCounter, true)
        agent.addBehaviour(behaviour)

        communicator.sendMessage(message())
        val succeeded = tryCounter.tryAcquire(2, retryTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)

        agent.removeBehaviour(behaviour)
    }

    @Test fun sendMessageShouldNotRetryIfSpecified() = test { agent.register(agentName, agentType)
        val tryCounter = Semaphore(0)
        val behaviour = receiveAndCount(tryCounter)
        agent.addBehaviour(behaviour)

        communicator.sendMessage(message(), false) { }
        val succeeded = tryCounter.tryAcquire(2, retryTime, TimeUnit.MILLISECONDS)
        Assert.assertFalse(succeeded)

        agent.removeBehaviour(behaviour)
    }

    private fun message() = ACLMessage(REQUEST).apply { addReceiver(agent.aid) }

    private fun receiveAndCount(counter: Semaphore, reply: Boolean = false) = cyclicBehaviour {
        agent.receive() ?.apply {
            if (reply) agent.send(createReply())
            counter.release()
        }
        it.block()
    }
}
