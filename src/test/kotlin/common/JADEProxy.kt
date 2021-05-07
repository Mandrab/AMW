package common

import controller.agent.AgentProxy
import jade.core.Agent
import java.util.concurrent.Semaphore

/**
 * Monitor proxy to communicate with the agent
 *
 * @author Paolo Baldini
 */
class JADEProxy<T: Agent>: AgentProxy<T> {
    private val semaphore = Semaphore(0)
    private var _agent: T? = null

    fun getAgent(): T {
        semaphore.acquire()
        return _agent!!
    }

    override fun setAgent(agent: T) {
        _agent = agent
        semaphore.release(Int.MAX_VALUE)
    }

    override fun isAvailable(): Boolean { return semaphore.availablePermits() > 0 }

    override fun shutdown() {
        semaphore.acquire()
        _agent!!.doDelete()
    }
}
