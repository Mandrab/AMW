package framework

import framework.AMWSpecificFramework.waitingTime
import jade.domain.DFService
import jason.infra.jade.JadeAgArch
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class ASLAgent: JadeAgArch() {

    override fun setup() {
        val monitor = arguments.first { it is AIDMonitor } as AIDMonitor
        monitor.agent = this
        super.setup()
    }

    override fun doDelete() {
        // the request is async with timeout in that seldom the asl agent consumes the confirmation instead of this
        // function (it is not thought to have other behaviours)
        try {
            CompletableFuture.runAsync {
                DFService.deregister(this, defaultDF)                       // try to deregister the agent
            }.get(waitingTime, TimeUnit.MILLISECONDS)
        } catch (_: Exception) { }
        super.doDelete()
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
