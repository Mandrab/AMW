package framework

import jason.infra.jade.JadeAgArch
import java.util.concurrent.Semaphore

class ASLAgent: JadeAgArch() {

    override fun setup() {
        val monitor = arguments.first { it is AIDMonitor } as AIDMonitor
        monitor.agent = this
        super.setup()
    }

    operator fun invoke(action: ASLAgent.() -> Unit) = run(action)

    class AIDMonitor {
        private val semaphore: Semaphore = Semaphore(0)
        private lateinit var _agent: ASLAgent
        var agent: ASLAgent
            get() {
                semaphore.acquire()
                return _agent
            }
            set(value) {
                _agent = value
                semaphore.release(Int.MAX_VALUE)
            }
    }
}
