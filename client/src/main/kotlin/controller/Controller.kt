package controller

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User as UserAbstraction
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

        fun placeOrder(user: UserAbstraction, elements: List<QuantityItem>)
    }

    operator fun invoke(role: SystemRoles, retryConnection: Boolean = true) = when (role) {
        SystemRoles.ADMIN -> AdminController(retryConnection)
        else -> UserController(retryConnection)
    }
}