package view

import controller.Controller

object View {

    interface View

    operator fun invoke(controller: Controller.Admin): View = TODO()

    operator fun invoke(controller: Controller.User): View = TODO()
}