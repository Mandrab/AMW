package controlled.agent.communication

import common.SupportAgent
import common.TestAgents.find
import common.TestAgents.proxy
import common.TestAgents.register
import controller.agent.Agents.cyclicBehaviour
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CommunicatorTest {
    private val waitingTime = 1000L

    private val communicatorName = "communicator-name" + Random.nextDouble()
    private val receiverName = "receiver-name" + Random.nextDouble()
    private val receiverType = "receiver-type"

    private lateinit var communicator: SupportAgent
    private lateinit var receiver: Agent

    @Before fun setup() {
        communicator = proxy(communicatorName, SupportAgent().javaClass.canonicalName).agent as SupportAgent
        receiver = proxy(receiverName).agent
        receiver.register(receiverName, receiverType)
    }

    @Test fun sendMessageEffectivelyDeliverIt() {
        val aid = communicator.find(receiverName, receiverType)
        communicator.sendMessage(ACLMessage().apply {
            content = "text"
            addReceiver(aid)
        })
        Assert.assertNotNull(receiver.blockingReceive(waitingTime))
    }

    @Test fun sendMessageAllowsToReceiveAResponse() {
        receiver.addBehaviour(cyclicBehaviour {
            receiver.send(receiver.blockingReceive().createReply())
        })
        val aid = communicator.find(receiverName, receiverType)
        val result = communicator.sendMessage(ACLMessage().apply {
            content = "text"
            addReceiver(aid)
        }).get(waitingTime, TimeUnit.MILLISECONDS)                          // throws an exception if timeout elapse
        Assert.assertNotNull(result)
    }
}
