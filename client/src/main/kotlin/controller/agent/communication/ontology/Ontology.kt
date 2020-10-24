package controller.agent.communication.ontology

import controller.agent.communication.ontology.dsl.abstraction.Address.Companion.address
import controller.agent.communication.ontology.dsl.abstraction.Client.Companion.client
import controller.agent.communication.ontology.dsl.abstraction.Command.Companion.command
import controller.agent.communication.ontology.dsl.abstraction.Description.Companion.description
import controller.agent.communication.ontology.dsl.abstraction.Email.Companion.email
import controller.agent.communication.ontology.dsl.abstraction.ID.Companion.command_id
import controller.agent.communication.ontology.dsl.abstraction.ID.Companion.id
import controller.agent.communication.ontology.dsl.abstraction.ID.Companion.order_id
import controller.agent.communication.ontology.dsl.abstraction.ID.Companion.v_id
import controller.agent.communication.ontology.dsl.abstraction.Item.item
import controller.agent.communication.ontology.dsl.abstraction.Name.Companion.name
import controller.agent.communication.ontology.dsl.abstraction.Position.Companion.position
import controller.agent.communication.ontology.dsl.abstraction.Quantity.Companion.quantity
import controller.agent.communication.ontology.dsl.abstraction.Rack.Companion.rack
import controller.agent.communication.ontology.dsl.abstraction.Requirement.Companion.requirement
import controller.agent.communication.ontology.dsl.abstraction.Script.Companion.script
import controller.agent.communication.ontology.dsl.abstraction.Shelf.Companion.shelf
import controller.agent.communication.ontology.dsl.abstraction.Variant.Companion.variant
import controller.agent.communication.ontology.dsl.operation.AddCommand
import controller.agent.communication.ontology.dsl.operation.AddItem
import controller.agent.communication.ontology.dsl.operation.AddVersion
import controller.agent.communication.ontology.dsl.operation.Execute.execute
import controller.agent.communication.ontology.dsl.operation.Order.Companion.order
import controller.agent.communication.ontology.dsl.operation.OrderInfo.Companion.info
import controller.agent.communication.ontology.dsl.operation.RemoveItem.Companion.remove
import controller.agent.communication.ontology.dsl.operation.RetrieveOrder.Companion.retrieve
import controller.agent.communication.ontology.Ontology.ServiceSupplier.*
import controller.agent.communication.ontology.Ontology.ServiceType.*
import common.Command
import common.Item.WarehouseItem
import common.Item.ShopItem
import common.Script.Script
import common.User.User
import jason.asSyntax.*

object Ontology {

    open class Service constructor(val serviceSupplier: String, val serviceType: String)

    val AcceptOrder = object: Service(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id) {
        fun parse(user: User, elements: List<ShopItem>): Literal =
            order(client(user.client()), email(user.email()), address(user.address()))[
                    elements.map { item(id(it.id()), quantity(it.quantity())) }
            ].term()
    }
    val AddCommand = object: Service(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id) {
        fun parse(command: Command) =
            AddCommand(command(id(command.id), name(command.name), description(command.description))).term()
    }
    val AddVersion = object: Service(MANAGEMENT_COMMANDS.id, ADD_VERSION.id) {
        fun parse(commandId: String, version: Command.Version) =
            AddVersion(
                    commandId,
                    variant(v_id(version.id), script(version.script), version.requirements.map { i -> requirement(i) })
            ).term()
    }
    val InfoCommands = object: Service(MANAGEMENT_COMMANDS.id, INFO_COMMANDS.id) {

    }
    val InfoOrders = object: Service(MANAGEMENT_ORDERS.id, INFO_ORDERS.id) {
        fun parse(user: User) = info(user.client(), user.email()).term()
    }
    val InfoWarehouse = object: Service(TODO(), INFO_WAREHOUSE.id) {

    }
    val ExecuteCommand = object: Service(EXECUTOR_COMMAND.id, EXEC_COMMAND.id) {
        fun parse(commandId: String) = execute(command_id(commandId)).term()
    }
    val ExecuteScript = object: Service(EXECUTOR_SCRIPT.id, EXEC_SCRIPT.id) {
        fun parse(script: Script) = execute(script(script.script())[
                script.requirements().map { requirement(it) }
        ]).term()
    }
    val RemoveItem = object: Service(TODO(), REMOVE_ITEM.id) {
        fun parse(item: WarehouseItem) =
            remove(item(
                    id(item.id()),
                    position(rack(item.rack()), shelf(item.shelf()), quantity(item.quantity()))
            )).term()
    }
    val RetrieveItems = object: Service(TODO(), RETRIEVE_ITEMS.id) {
        fun parse(orderId: String, elements: List<ShopItem>) = retrieve(order_id(orderId))[
                elements.map { item(id(it.id()), quantity(it.quantity())) }
        ].term()
    }
    val RetrieveItem = object: Service(TODO(), RETRIEVE_ITEM.id) {

    }
    val StoreItem = object: Service(TODO(), STORE_ITEM.id) {
        fun parse(item: WarehouseItem) =
            AddItem(item(
                    id(item.id()),
                    position(rack(item.rack()), shelf(item.shelf()), quantity(item.quantity()))
            )).term()
    }

    /**
     * Possible services type in the system
     *
     * @author Paolo Baldini
     */
    private enum class ServiceSupplier(val id: String) {
        MANAGEMENT_COMMANDS("management(commands)"),
        MANAGEMENT_ITEMS("management(items)"),
        MANAGEMENT_ORDERS("management(orders)"),
        EXECUTOR_COMMAND("executor(command)"),
        EXECUTOR_SCRIPT("executor(command)"),
        PICKER_ITEMS("executor(item_picker)")
    }

    private enum class ServiceType(val id: String) {
        ACCEPT_ORDER("accept(order)"),
        ADD_COMMAND("add(command)"),
        ADD_VERSION("add(version)"),
        INFO_COMMANDS("info(commands)"),
        INFO_ORDERS("info(orders)"),
        INFO_WAREHOUSE("info(warehouse)"),
        EXEC_COMMAND("exec(command)"),
        EXEC_SCRIPT("exec(command)"),
        REMOVE_ITEM("remove(item)"),
        RETRIEVE_ITEMS("retrieve(item)"),
        RETRIEVE_ITEM("retrieve(item)"),
        STORE_ITEM("store(item)");
    }
}