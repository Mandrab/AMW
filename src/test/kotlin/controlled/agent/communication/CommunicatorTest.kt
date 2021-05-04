package controlled.agent.communication

import common.SupportAgent
import common.TestAgents.proxy
import common.TestAgents.register
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CommunicatorTest {
    private val waitingTime = 500L

    private val communicatorName = "communicator-name"
    private val receiverName = "receiver-name"
    private val receiverType = "receiver-type"

    private lateinit var communicator: SupportAgent
    private lateinit var receiver: Agent

    @Before fun setup() {
        communicator = proxy(communicatorName, SupportAgent().javaClass.canonicalName).agent as SupportAgent
        receiver = proxy(receiverName).agent
        receiver.register(receiverName, receiverType)
    }

    @Test fun sendMessageEffectivelyDeliverIt() {
        val aid = communicator.run {
            DFService.search(this, DFAgentDescription().apply {
                addServices(ServiceDescription().apply { name = receiverName })
            })
        }.first().name
        communicator.sendMessage(ACLMessage().apply {
            content = "text"
            addReceiver(aid)
        })
        Assert.assertNotNull(receiver.blockingReceive(waitingTime))
    }
}
