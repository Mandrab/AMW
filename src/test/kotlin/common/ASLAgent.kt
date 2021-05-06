package common

import jade.core.AID
import jason.infra.jade.JadeAgArch
import java.util.concurrent.Semaphore

class ASLAgent: JadeAgArch() {

    override fun setup() {
        val monitor = arguments[1] as AIDMonitor
        monitor.aid = aid
        super.setup()
    }

    class AIDMonitor {
        private val semaphore: Semaphore = Semaphore(0)
        private var _aid: AID = AID()
        var aid: AID
            get() {
                semaphore.acquire()
                return _aid
            }
            set(value) {
                _aid = value
                semaphore.release(Int.MAX_VALUE)
            }
    }
}
