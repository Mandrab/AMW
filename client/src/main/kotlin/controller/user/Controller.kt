package controller.user

import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.user
import common.ontology.dsl.operation.Order.Order
import controller.Controller
import controller.agent.Agents
import controller.user.agent.Agent
import controller.user.agent.Proxy
import view.View

/**
 * Main class of the application that creates agent and manage main data flow
 *
 * @param retryConnection run application keeping retry connection if it fails
 *
 * @author Paolo Baldini
 */
class Controller(retryConnection: Boolean = true): Controller.User {
    private val client = "user"
    private val email = "user@email"
    private val address = "address"

    private val proxy = Proxy()

    init {
        Agents.start(retryConnection)(arrayOf(Proxy(), client, email))(Agent::class.java)
        View(this)
    }

    override fun shopItems(): Collection<QuantityItem> = emptyList()

    override fun placeOrder(items: Collection<QuantityItem>) =
            proxy.placeOrder(user(client(client), email(email), address(address)), items)

    override fun orders(): Collection<Order> = emptyList()

    override fun stopSystem() = proxy.shutdown()
}