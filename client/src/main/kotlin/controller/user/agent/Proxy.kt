package controller.user.agent

import controller.agent.AgentProxy

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun placeOrder()
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private lateinit var agent: Agent

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = this::agent.isInitialized

        override fun shutdown() = agent.shutdown()

        override fun placeOrder() = agent.placeOrder()
    }
}