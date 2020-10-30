package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.Order
import controller.agent.AgentProxy
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun shopItems(): Collection<QuantityItem>

        fun placeOrder(user: User, elements: Collection<QuantityItem>)

        fun orders(user: User): Future<Collection<Order>>
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private lateinit var agent: Agent

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = this::agent.isInitialized

        override fun shutdown() = agent.shutdown()

        override fun shopItems() = agent.shopItems()

        override fun placeOrder(user: User, elements: Collection<QuantityItem>) = agent.placeOrder(user, elements)

        override fun orders(user: User) = agent.orders(user)
    }
}