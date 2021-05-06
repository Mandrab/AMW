package controlled.agent.communication

import controller.agent.Communicator
import common.JADEAgents.TestProxy

class Communicator: Communicator() {

    override fun setup() {
        super.setup()
        (arguments[0] as TestProxy<controlled.agent.communication.Communicator>).agent = this
    }
}