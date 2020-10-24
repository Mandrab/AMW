package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import controller.agent.communication.translation.out.Services.AcceptOrder
import controller.user.agent.Proxy.Proxy
import jade.core.Agent as JadeAgent

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class Agent: JadeAgent() {

    override fun setup() {
        super.setup()
        (arguments[0] as Proxy).setAgent(this)
    }

    fun shutdown() = takeDown()

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(user: User, elements: List<QuantityItem>) = send(AcceptOrder.build(user, elements).message())
}