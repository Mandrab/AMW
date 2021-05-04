package common

import jade.core.Agent

class TestAgent: Agent() {
    override fun setup() {
        super.setup()
        (arguments[0] as TestAgents.TestProxy<TestAgent>).agent = this
    }
}
