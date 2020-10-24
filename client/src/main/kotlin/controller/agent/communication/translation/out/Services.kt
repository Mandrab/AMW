package controller.agent.communication.translation.out

import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.Command.CommandInfo
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.abstraction.Variant.Variant
import common.ontology.dsl.operation.AddCommand.add
import common.ontology.dsl.operation.AddVersion.add
import common.ontology.dsl.operation.AddItem.add
import common.ontology.dsl.operation.Execute.execute
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.OrderInfo.info
import common.ontology.dsl.operation.RemoveItem.remove
import common.ontology.dsl.operation.RetrieveOrder.retrieve
import controller.agent.communication.Messages.message
import controller.agent.communication.Messages.receiver
import controller.agent.communication.translation.out.OperationTerms.term
import jade.lang.acl.ACLMessage
import jason.asSyntax.*

object Services {

    abstract class Service constructor(
            val serviceSupplier: String,
            val serviceType: String,
            val servicePerformative: Int = TODO()
    ) {
        abstract fun parse(): Literal

        fun message(): ACLMessage =
            message {
                performative = servicePerformative
                payloadObject = parse()
                receivers {
                    receiver {
                        supplier = serviceSupplier
                        service = serviceType
                    }
                }
            }
    }

    object AcceptOrder {
        fun build(user: User, elements: List<QuantityItem>) = object: Service(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id) {
            override fun parse(): Literal = order(user.client, user.email, user.address)[elements].term()
        }
    }

    object AddCommand {
        fun build(command: CommandInfo) = object: Service(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id) {
            override fun parse(): Literal = add(command).term()
        }
    }

    object AddVersion {
        fun build(commandId: String, version: Variant) = object: Service(MANAGEMENT_COMMANDS.id, ADD_VERSION.id) {
            override fun parse(): Literal = add(commandId, version).term()
        }
    }

    object InfoCommands//: Service(MANAGEMENT_COMMANDS.id, INFO_COMMANDS.id)

    object InfoOrders {
        fun build(user: User) = object: Service(MANAGEMENT_ORDERS.id, INFO_ORDERS.id) {
            override fun parse(): Literal = info(user.client, user.email).term()
        }
    }

    object InfoWarehouse//: Service(TODO(), INFO_WAREHOUSE.id)

    object ExecuteCommand {
        fun build(commandId: ID) = object: Service(EXECUTOR_COMMAND.id, EXEC_COMMAND.id) {
            override fun parse(): Literal = execute(commandId).term()
        }
    }

    object ExecuteScript {
        fun build(script: Script) = object: Service(EXECUTOR_SCRIPT.id, EXEC_SCRIPT.id) {
            override fun parse(): Literal = execute(script).term()
        }
    }

    object RemoveItem {
        fun build(item: WarehouseItem) = object: Service(TODO(), REMOVE_ITEM.id) {
            override fun parse(): Literal = remove(item).term()
        }
    }

    object RetrieveItems {
        fun build(orderId: ID, elements: List<QuantityItem>) = object: Service(TODO(), RETRIEVE_ITEMS.id) {
            override fun parse(): Literal = retrieve(orderId)[elements].term()
        }
    }

    object RetrieveItem//: Service(TODO(), RETRIEVE_ITEM.id)

    object StoreItem {
        fun build(item: WarehouseItem) = object: Service(TODO(), STORE_ITEM.id) {
            override fun parse(): Literal = add(item).term()
        }
    }
}