package common

import jade.core.AID
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import jason.asSyntax.Literal

class JADEAgent: Agent() {
    override fun setup() {
        super.setup()
        @Suppress("UNCHECKED_CAST")
        (arguments[0] as JADEProxy<JADEAgent>).setAgent(this)
    }

    fun register(_name: String, vararg types: String) = this.apply {
        DFService.register(this, defaultDF, DFAgentDescription().apply {
            types.map {
                addServices(ServiceDescription().apply { name = _name; type = it })
            }
        })
    }

    fun deregister() = DFService.deregister(this, defaultDF)

    fun sendRequest(message: Literal, receiver: AID, performative: Int = ACLMessage.REQUEST) =
        sendRequest(message.toString(), receiver, performative)

    fun sendRequest(message: String, receiver: AID, performative: Int = ACLMessage.REQUEST) =
        send(ACLMessage(performative).apply {
            addReceiver(receiver)
            content = message
        })

    operator fun invoke(action: JADEAgent.() -> Unit) = run(action)
}
