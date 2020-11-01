package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.PlaceOrder
import controller.agent.AgentProxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun shopItems(): Future<Collection<QuantityItem>>

        fun placeOrder(user: User, items: Collection<QuantityItem>)

        fun orders(user: User): Future<Collection<PlaceOrder>>
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private var agent: Agent? = null

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = agent != null

        override fun shutdown() = agent?.shutdown() ?: Unit

        override fun shopItems() = agent?.shopItems() ?: future(emptyList())

        override fun placeOrder(user: User, items: Collection<QuantityItem>) = agent?.placeOrder(user, items) ?: Unit

        override fun orders(user: User) = agent?.orders(user) ?: future(emptyList())

        private fun <T> future(element: T): Future<T> = CompletableFuture.completedFuture(element)
    }
}