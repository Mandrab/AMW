package controller.user.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Order.InfoOrder
import controller.agent.Communicator
import controller.agent.communication.translation.`in`.Services.InfoWarehouse as InfoWarehouseIn
import controller.agent.communication.translation.out.Services.InfoWarehouse as InfoWarehouseOut
import controller.agent.communication.translation.`in`.Services.InfoOrders as InfoOrdersIn
import controller.agent.communication.translation.out.Services.InfoOrders as InfoOrdersOut
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

    fun shopItems(): Future<Collection<QuantityItem>> = sendMessage(InfoWarehouseOut.build().message(this)) {
        InfoWarehouseIn.parse(it).map { item ->
            item(item.id, quantity(item.positions.map { pos -> pos.quantity.value }.sum()))
        }.filter { it.quantity.value > 0 }
    }

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(user: User, items: Collection<QuantityItem>) = send(AcceptOrder.build(user, items).message(this))

    fun orders(user: User): Future<Collection<InfoOrder>> =
        sendMessage(InfoOrdersOut.build(user).message(this), true, InfoOrdersIn.parse)
}