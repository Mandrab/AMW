package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.Order
import controller.agent.AgentProxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun shopItems(): Collection<QuantityItem>

        fun placeOrder(user: User, elements: Collection<QuantityItem>)

        fun orders(user: User): Future<Collection<Order>>
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private var agent: Agent? = null

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = agent != null

        override fun shutdown() = agent?.shutdown() ?: Unit

        override fun shopItems() = agent?.shopItems() ?: emptyList()

        override fun placeOrder(user: User, elements: Collection<QuantityItem>) =
                agent?.placeOrder(user, elements) ?: Unit

        override fun orders(user: User) = agent?.orders(user) ?: CompletableFuture.completedFuture(emptyList())
    }
}