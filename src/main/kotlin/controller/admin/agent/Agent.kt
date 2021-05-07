package controller.admin.agent

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import controller.agent.AgentProxy
import controller.agent.Communicator
import controller.agent.communication.translation.`in`.Services.InfoWarehouse as InfoWarehouseIn
import controller.agent.communication.translation.out.Services.InfoWarehouse as InfoWarehouseOut
import controller.agent.communication.translation.out.Services.StoreItem as StoreItemOut
import controller.agent.communication.translation.out.Services.RemoveItem as RemoveItemOut
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
        @Suppress("UNCHECKED_CAST")
        (arguments[0] as AgentProxy<Agent>).setAgent(this)
    }

    fun shutdown() = super.takeDown()

    fun addCommand() { TODO() }

    fun addItem(item: WarehouseItem) { sendMessage(StoreItemOut.build(item).message(this), true) { } }

    fun removeItem(item: QuantityItem) { sendMessage(RemoveItemOut.build(item).message(this), true) { } }

    fun addVersion() { TODO() }

    fun executeCommand() { TODO() }

    fun executeScript() { TODO() }

    fun warehouseState(): Future<Collection<Product>> =
            sendMessage(InfoWarehouseOut.build().message(this), true, InfoWarehouseIn.parse)
}