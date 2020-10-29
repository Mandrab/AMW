package controller

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.operation.Order.Order
import controller.admin.Controller as AdminController
import controller.user.Controller as UserController

object Controller {

    interface Controller {

        fun stopSystem()
    }

    interface Admin: Controller {

        fun addCommand()

        fun addVersion()

        fun executeCommand()

        fun executeScript()
    }

    interface User: Controller {

        fun shopItems(): Collection<QuantityItem>

        fun placeOrder(items: Collection<QuantityItem>)

        fun orders(): Collection<Order>
    }

    operator fun invoke(role: SystemRoles, retryConnection: Boolean = true) = when (role) {
        SystemRoles.ADMIN -> AdminController(retryConnection)
        else -> UserController(retryConnection)
    }
}