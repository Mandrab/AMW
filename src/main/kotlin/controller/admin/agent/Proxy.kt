package controller.admin.agent

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import controller.agent.AgentProxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        /**
         * Add a command in the repository
         */
        fun addCommand(command: Command)

        /**
         * Add an item in the warehouse
         */
        fun addItem(item: WarehouseItem)

        /**
         * Require list of commands from repository
         */
        fun commandsList(): Future<Collection<Command>>

        /**
         * Ask for command execution
         */
        fun executeCommand(id: ID)

        /**
         * Remove an item from the warehouse
         */
        fun removeItem(item: QuantityItem)

        /**
         * Require warehouse state from warehouse manager
         */
        fun warehouseState(): Future<Collection<Product>>
    }

    operator fun invoke(): Proxy = object: Proxy {
        private var agent: Agent? = null                                    // reference to the agent entity

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
         * Add a command in the repository
         */
        override fun addCommand(command: Command) = agent?.addCommand(command) ?: Unit

        /**
         * Add an item in the warehouse
         */
        override fun addItem(item: WarehouseItem) = agent?.addItem(item) ?: Unit

        /**
         * Require list of commands from repository
         */
        override fun commandsList() = agent?.commandsList() ?: future(emptyList())

        /**
         * Ask for command execution
         */
        override fun executeCommand(id: ID) = agent?.executeCommand(id) ?: Unit

        /**
         * Remove an item from the warehouse
         */
        override fun removeItem(item: QuantityItem) = agent?.removeItem(item) ?: Unit

        /**
         * Require warehouse state from warehouse manager
         */
        override fun warehouseState() = agent?.warehouseState() ?: future(emptyList())

        private fun <T> future(element: T): Future<T> = CompletableFuture.completedFuture(element)
    }
}
