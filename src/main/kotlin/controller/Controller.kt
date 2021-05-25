package controller

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.operation.Order.InfoOrder
import java.util.concurrent.Future
import controller.admin.Controller as AdminController
import controller.user.Controller as UserController

/**
 * Generic controller object
 * Defines the structure of the admin and user controllers
 *
 * @author Paolo Baldini
 */
object Controller {

    interface Controller {

        /**
         * Shutdown the agent
         */
        fun stopSystem()
    }

    interface Admin: Controller {

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

    interface User: Controller {

        /**
         * Require warehouse items
         */
        fun shopItems(): Future<Collection<QuantityItem>>

        /**
         * Allows to place an order with submitted elements
         */
        fun placeOrder(items: Collection<QuantityItem>)

        /**
         * Require orders information
         */
        fun orders(): Future<Collection<InfoOrder>>
    }

    /**
     * Returns the correct controller implementation
     */
    operator fun invoke(role: SystemRoles, retryConnection: Boolean = true) = when (role) {
        SystemRoles.ADMIN -> AdminController(retryConnection)
        else -> UserController(retryConnection)
    }
}
