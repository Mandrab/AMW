package controller

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

    interface Client: Controller {

        fun placeOrder()
    }

    operator fun invoke(roles: SystemRoles, retryConnection: Boolean = true): Nothing = when (roles) {
        SystemRoles.ADMIN -> TODO()
        else -> TODO()
    }
}