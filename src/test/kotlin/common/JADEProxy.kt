package common

import jade.core.Agent
import java.util.concurrent.Semaphore

/**
 * Monitor proxy to communicate with the agent
 *
 * @author Paolo Baldini
 */
class JADEProxy<T: Agent> {
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