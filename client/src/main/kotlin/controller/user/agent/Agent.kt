package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.PlaceOrder
import controller.agent.Communicator
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.`in`.OperationTerms.parse
import controller.agent.communication.translation.out.Services.InfoOrders
import controller.agent.communication.translation.out.Services.AcceptOrder
import controller.user.agent.Proxy.Proxy
import java.util.concurrent.Future

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class Agent: Communicator() {

    override fun setup() {
        super.setup()
        (arguments[0] as Proxy).setAgent(this)
    }

    fun shutdown() = takeDown()

    fun shopItems(): Collection<QuantityItem> = emptyList()//TODO

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(user: User, elements: Collection<QuantityItem>) = send(AcceptOrder.build(user, elements).message())

    fun orders(user: User): Future<Collection<PlaceOrder>> = sendMessage(InfoOrders.build(user).message()) {
        it.content.asList().map { order -> PlaceOrder.parse(order) }
    }
}