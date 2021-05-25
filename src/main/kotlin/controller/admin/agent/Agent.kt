package controller.admin.agent

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import controller.agent.AgentProxy
import controller.agent.Communicator
import controller.agent.communication.translation.`in`.Services.InfoCommands as InfoCommandsIn
import controller.agent.communication.translation.`in`.Services.InfoWarehouse as InfoWarehouseIn
import controller.agent.communication.translation.out.Services.AddCommand
import controller.agent.communication.translation.out.Services.ExecuteCommand
import controller.agent.communication.translation.out.Services.InfoCommands as InfoCommandsOut
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

    /**
     * Add a command in the repository
     */
    fun addCommand(command: Command) { sendMessage(AddCommand.build(command).message(this)) }

    /**
     * Add an item in the warehouse
     */
    fun addItem(item: WarehouseItem) { sendMessage(StoreItemOut.build(item).message(this)) }

    /**
     * Require list of commands from repository
     */
    fun commandsList(): Future<Collection<Command>> =
        sendMessage(InfoCommandsOut.build().message(this), true, InfoCommandsIn.parse)

    /**
     * Ask for command execution
     */
    fun executeCommand(id: ID) { sendMessage(ExecuteCommand.build(id).message(this)) }

    /**
     * Remove an item from the warehouse
     */
    fun removeItem(item: QuantityItem) { sendMessage(RemoveItemOut.build(item).message(this)) }

    /**
     * Require warehouse state from warehouse manager
     */
    fun warehouseState(): Future<Collection<Product>> =
            sendMessage(InfoWarehouseOut.build().message(this), true, InfoWarehouseIn.parse)
}
