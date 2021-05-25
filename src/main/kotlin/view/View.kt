package view

import controller.Controller

/**
 * Define a way to provide the correct view given a specific controller
 *
 * @author Paolo Baldini
 */
object View {

    operator fun invoke(controller: Controller.Admin) = view.admin.View(controller)

    operator fun invoke(controller: Controller.User) = view.user.View(controller)
}
