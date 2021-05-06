package common

import jade.core.Agent
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import java.util.concurrent.Semaphore
import kotlin.random.Random

object JADEAgents {

    /** Monitor proxy to communicate with the agent */
    class TestProxy<T: Agent> {
        private val semaphore = Semaphore(0)
        private var _agent: T? = null
        var agent: T
            get() {
                semaphore.acquire()
                return _agent!!
            }
            set(value) {
                _agent = value
                semaphore.release(Int.MAX_VALUE)
            }
    }

    fun proxy(name: String, _class: String = JADEAgent().javaClass.canonicalName) = TestProxy<Agent>().apply {
        Runtime.instance()
            .createAgentContainer(ProfileImpl())
            .createNewAgent(name + Random.nextDouble(), _class, arrayOf(this))
            .run { start() }
    }

    fun Agent.register(_name: String, vararg types: String) {
        DFService.register(this, defaultDF, DFAgentDescription().apply {
            types.map {
                addServices(ServiceDescription().apply { name = _name; type = it })
            }
        })
    }
}
