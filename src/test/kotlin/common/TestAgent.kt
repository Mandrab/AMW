package tester.asl.order_manager

import jade.core.Agent

class TestAgent: Agent() {
    class Proxy {
        private var _agent: TestAgent? = null
        var agent: TestAgent
            get() {
                while (_agent == null);
                return _agent!!
            }
            set(value) { _agent = value }
    }

    override fun setup() {
        super.setup()
        (arguments[0] as Proxy).agent = this
    }
}