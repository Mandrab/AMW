package common

import jade.core.Agent

class JADEAgent: Agent() {
    override fun setup() {
        super.setup()
        (arguments[0] as JADEAgents.TestProxy<JADEAgent>).agent = this
    }
}
