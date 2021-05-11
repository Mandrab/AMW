package framework

import jade.core.AID
import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.lang.acl.ACLMessage
import jason.asSyntax.Literal
import kotlin.random.Random

class JADEAgent: Agent() {
    private var registered = false

    override fun setup() {
        super.setup()
        @Suppress("UNCHECKED_CAST")
        (arguments[0] as JADEProxy<JADEAgent>).setAgent(this)
    }

    fun register(_name: String, vararg types: String) = this.apply {
        registered = true
        DFService.register(this, defaultDF, DFAgentDescription().apply {
            types.map {
                addServices(ServiceDescription().apply { name = _name; type = it })
            }
        })
    }

    fun deregister() {
        if (registered) DFService.deregister(this, defaultDF)
        registered = false
    }

    fun sendRequest(message: Literal, receiver: AID, performative: Int = ACLMessage.REQUEST) =
        apply { sendRequest(message.toString(), receiver, performative) }

    fun sendRequest(message: String, receiver: AID, performative: Int = ACLMessage.REQUEST) =
        apply { send(ACLMessage(performative).apply {
            addReceiver(receiver)
            content = message
            replyWith = Random.nextDouble().toString()
        }) }

    operator fun invoke(action: JADEAgent.() -> Unit) = run(action)

    override fun doDelete() {
        deregister()
        super.doDelete()
    }
}
