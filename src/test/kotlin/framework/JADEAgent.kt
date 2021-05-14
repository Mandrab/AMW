package framework

import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription

class JADEAgent: Agent() {
    private var registered = false

    override fun setup() {
        super.setup()
        @Suppress("UNCHECKED_CAST")
        (arguments[0] as JADEProxy<JADEAgent>).setAgent(this)
    }

    override fun doDelete() {
        if (registered) DFService.deregister(this, defaultDF)
        registered = false
        super.doDelete()
    }

    fun register(_name: String, vararg types: String) = this.apply {
        registered = true
        DFService.register(this, defaultDF, DFAgentDescription().apply {
            types.map {
                addServices(ServiceDescription().apply { name = _name; type = it })
            }
        })
    }

    operator fun invoke(action: JADEAgent.() -> Unit) = run(action)
}
