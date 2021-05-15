package controller.admin.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import controller.agent.AgentProxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun addCommand()

        fun addItem(item: WarehouseItem)

        fun removeItem(item: QuantityItem)

        fun executeCommand()

        fun warehouseState(): Future<Collection<Product>>
    }

    operator fun invoke(): Proxy = AdminProxy()

    private class AdminProxy: Proxy {
        private var agent: Agent? = null

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = agent != null

        override fun shutdown() = agent?.shutdown() ?: Unit

        override fun addCommand() = agent?.addCommand() ?: Unit

        override fun addItem(item: WarehouseItem) = agent?.addItem(item) ?: Unit

        override fun removeItem(item: QuantityItem) = agent?.removeItem(item) ?: Unit

        override fun executeCommand() = agent?.executeCommand() ?: Unit

        override fun warehouseState() = agent?.warehouseState() ?: future(emptyList())

        private fun <T> future(element: T): Future<T> = CompletableFuture.completedFuture(element)
    }
}