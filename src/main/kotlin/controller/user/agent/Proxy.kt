package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.InfoOrder
import controller.agent.AgentProxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        /**
         * Require warehouse items
         */
        fun shopItems(): Future<Collection<QuantityItem>>

        /**
         * Allows to place an order with submitted elements
         */
        fun placeOrder(user: User, items: Collection<QuantityItem>)

        /**
         * Require orders information
         */
        fun orders(user: User): Future<Collection<InfoOrder>>
    }

    operator fun invoke(): Proxy = ClientProxy()

    private class ClientProxy: Proxy {
        private var agent: Agent? = null

        /**
         * Set agent to be the proxy of
         */
        override fun setAgent(agent: Agent) { this.agent = agent }

        /**
         * Says if proxy is set and available
         */
        override fun isAvailable() = agent != null

        /**
         * Shutdown the agent
         */
        override fun shutdown() = agent?.shutdown() ?: Unit

        /**
         * Require warehouse items
         */
        override fun shopItems() = agent?.shopItems() ?: future(emptyList())

        /**
         * Allows to place an order with submitted elements
         */
        override fun placeOrder(user: User, items: Collection<QuantityItem>) = agent?.placeOrder(user, items) ?: Unit

        /**
         * Require orders information
         */
        override fun orders(user: User) = agent?.orders(user) ?: future(emptyList())

        private fun <T> future(element: T): Future<T> = CompletableFuture.completedFuture(element)
    }
}
