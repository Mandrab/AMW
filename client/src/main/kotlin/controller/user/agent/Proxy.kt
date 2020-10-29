package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import controller.agent.AgentProxy

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun placeOrder(user: User, elements: Collection<QuantityItem>)
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private lateinit var agent: Agent

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = this::agent.isInitialized

        override fun shutdown() = agent.shutdown()

        override fun placeOrder(user: User, elements: Collection<QuantityItem>) = agent.placeOrder(user, elements)
    }
}