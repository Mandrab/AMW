package controller

import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.operation.Order.InfoOrder
import java.util.concurrent.Future
import controller.admin.Controller as AdminController
import controller.user.Controller as UserController

object Controller {

    interface Controller {

        fun stopSystem()
    }

    interface Admin: Controller {

        fun addCommand()

        fun addItem(item: WarehouseItem)

        fun removeItem(item: QuantityItem)

        fun executeCommand()

        fun warehouseState(): Future<Collection<Product>>
    }

    interface User: Controller {

        fun shopItems(): Future<Collection<QuantityItem>>

        fun placeOrder(items: Collection<QuantityItem>)

        fun orders(): Future<Collection<InfoOrder>>
    }

    operator fun invoke(role: SystemRoles, retryConnection: Boolean = true) = when (role) {
        SystemRoles.ADMIN -> AdminController(retryConnection)
        else -> UserController(retryConnection)
    }
}