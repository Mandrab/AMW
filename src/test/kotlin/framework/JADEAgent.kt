package framework

import jade.core.Agent
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription

/**
 * Utilities for the start of a jade agent from it's main class
 *
 * @author Paolo Baldini
 */
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

    fun register(_name: String, vararg types: String) = register(*types.map { _name to it }.toTypedArray())

    fun register(vararg descriptions: Pair<String, String>) = this.apply {
        registered = true
        DFService.register(this, defaultDF, DFAgentDescription().apply {
            descriptions.forEach {
                addServices(ServiceDescription().apply { name = it.first; type = it.second })
            }
        })
    }

    operator fun invoke(action: JADEAgent.() -> Unit) = run(action)
}
