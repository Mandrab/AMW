package controller.user.agent

import controller.agent.AgentProxy
import jade.core.Agent

object Proxy {

    interface Proxy: AgentProxy {

        fun placeOrder()

        fun shutdown()
    }

    operator fun invoke(): Proxy = ClientProxy()

    /**
     * Client-agent proxy class (proxy pattern)
     * It allows to communicate to agent through this
     *
     * @author Paolo Baldini
     */
    private class ClientProxy: Proxy {
        private lateinit var agent: controller.user.agent.Agent

        /** {@inheritDoc} */
        override fun setAgent(agent: Agent) { this.agent = agent as controller.user.agent.Agent
        }

        /** {@inheritDoc} */
        override fun isAvailable() = this::agent.isInitialized

        override fun placeOrder() = agent.placeOrder()

        override fun shutdown() = agent.shutdown()
    }
}