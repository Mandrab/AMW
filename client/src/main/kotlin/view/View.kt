package view

import controller.Controller

object View {

    operator fun invoke(controller: Controller.Admin) = view.admin.View(controller)

    operator fun invoke(controller: Controller.User) = view.user.View(controller)
}