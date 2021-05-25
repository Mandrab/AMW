package controller.agent.communication

import framework.JADEProxy
import controller.agent.Communicator

/**
 * Fake agent for testing
 *
 * @author Paolo Baldini
 */
class Communicator: Communicator() {

    override fun setup() {
        super.setup()
        @Suppress("UNCHECKED_CAST")
        (arguments[0] as JADEProxy<Communicator>).setAgent(this)
    }
}
