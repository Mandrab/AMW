package controller.user

import controller.Controller.User

/**
 * Main class of the application that creates agent and manage main data flow
 *
 * @param retryConnection run application keeping retry connection if it fails
 *
 * @author Paolo Baldini
 */
class Controller(retryConnection: Boolean = true): User {

    override fun placeOrder() {
        TODO("Not yet implemented")
    }

    override fun stopSystem() {
        TODO("Not yet implemented")
    }
}