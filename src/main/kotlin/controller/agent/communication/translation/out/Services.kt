package controller.agent.communication.translation.out

import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.operation.Command.add
import common.ontology.dsl.operation.Command.execute
import common.ontology.dsl.operation.Info.Target.COMMANDS
import common.ontology.dsl.operation.Item.add
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.Item.remove
import common.ontology.dsl.operation.Info.Target.WAREHOUSE
import common.ontology.dsl.operation.Info.info
import controller.agent.communication.Messages.message
import controller.agent.communication.Messages.receiver
import controller.agent.communication.translation.out.OperationTerms.term
import jade.core.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.REQUEST
import jade.lang.acl.ACLMessage.INFORM
import jason.asSyntax.*

object Services {

    abstract class Service constructor(
        private val serviceSupplier: String,
        private val serviceType: String,
        private val servicePerformative: Int = INFORM
    ) {
        abstract fun parse(): Literal

        fun message(agent: Agent): ACLMessage =
            message {
                performative = servicePerformative
                payloadObject = parse()
                receivers(
                    receiver {
                        supplier = serviceSupplier
                        service = serviceType
                    }
                )
            }(agent)
    }

    object AcceptOrder {
        fun build(user: User, items: Collection<QuantityItem>) =
                object: Service(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id, REQUEST) {
                        override fun parse(): Literal = order(user.client, user.email, user.address)[items].term()
                }
    }

    object AddCommand {
        fun build(command: Command) = object: Service(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id, REQUEST) {
            override fun parse(): Literal = add(command).term()
        }
    }

    object InfoCommands{
        fun build() = object: Service(MANAGEMENT_COMMANDS.id, INFO_COMMANDS.id, REQUEST) {
            override fun parse(): Literal = info(COMMANDS).term()
        }
    }

    object InfoOrders {
        fun build(user: User) = object: Service(MANAGEMENT_ORDERS.id, INFO_ORDERS.id, REQUEST) {
            override fun parse(): Literal = info(user.client, user.email).term()
        }
    }

    object InfoWarehouse {
        fun build() = object: Service(MANAGEMENT_ITEMS.id, INFO_WAREHOUSE.id, REQUEST) {
            override fun parse(): Literal = info(WAREHOUSE).term()
        }
    }

    object ExecuteCommand {
        fun build(id: ID) = object: Service(EXECUTOR_COMMAND.id, EXEC_COMMAND.id, REQUEST) {
            override fun parse(): Literal = execute(id).term()
        }
    }

    object RemoveItem {
        fun build(item: QuantityItem) = object: Service(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id, REQUEST) {
            override fun parse(): Literal = remove(item).term()
        }
    }

    object StoreItem {
        fun build(item: WarehouseItem) = object: Service(MANAGEMENT_ITEMS.id, STORE_ITEM.id, REQUEST) {
            override fun parse(): Literal = add(item).term()
        }
    }
}
