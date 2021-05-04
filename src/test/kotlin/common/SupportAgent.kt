package common

import controller.agent.Communicator
import common.TestAgents.TestProxy

class SupportAgent: Communicator() {

    override fun setup() {
        super.setup()
        (arguments[0] as TestProxy<SupportAgent>).agent = this
    }
}