package common

import controller.agent.Communicator
import common.TestAgents.TestProxy

class Communicator: Communicator() {

    override fun setup() {
        super.setup()
        (arguments[0] as TestProxy<common.Communicator>).agent = this
    }
}