package controller.user

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import controller.Controller
import controller.agent.Agents
import controller.user.agent.Agent
import controller.user.agent.Proxy

/**
 * Main class of the application that creates agent and manage main data flow
 *
 * @param retryConnection run application keeping retry connection if it fails
 *
 * @author Paolo Baldini
 */
class Controller(retryConnection: Boolean = true): Controller.User {
    private val proxy = Proxy()

    init {
        Agents.start(retryConnection)(arrayOf(Proxy(), "user", "user@mail"))(Agent::class.java)
    }

    override fun placeOrder(user: User, elements: List<QuantityItem>) = proxy.placeOrder(user, elements)

    override fun stopSystem() = proxy.shutdown()
}