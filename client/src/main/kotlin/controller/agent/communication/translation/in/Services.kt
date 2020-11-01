package controller.agent.communication.translation.`in`

import common.ontology.Services
import common.ontology.Services.ServiceSupplier.*
import common.ontology.Services.ServiceType.*
import common.ontology.dsl.abstraction.Command.CommandInfo
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.abstraction.Variant.Variant
import common.ontology.dsl.operation.Command.add
import common.ontology.dsl.operation.Command.execute
import common.ontology.dsl.operation.Version.add
import common.ontology.dsl.operation.Item.add
import common.ontology.dsl.operation.Script.execute
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.Item.remove
import common.ontology.dsl.operation.Warehouse.Target.WAREHOUSE
import common.ontology.dsl.operation.Warehouse.info
import controller.agent.communication.Messages.message
import controller.agent.communication.Messages.receiver
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.out.OperationTerms.term
import controller.user.agent.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.ACLMessage.REQUEST
import jade.lang.acl.ACLMessage.INFORM
import jason.asSyntax.*

object Services {

    interface Service<T> {
        val parse: (message: ACLMessage) -> T
    }

    object AcceptOrder {
        fun build(user: User, items: Collection<QuantityItem>): Nothing  = TODO()
    }

    object AddCommand {
        fun build(command: CommandInfo): Nothing  = TODO()
    }

    object AddVersion {
        fun build(commandId: String, version: Variant): Nothing  = TODO()
    }

    object InfoCommands//: Service(MANAGEMENT_COMMANDS.id, INFO_COMMANDS.id)

    object InfoOrders {
        fun build(user: User): Nothing  = TODO()
    }

    object InfoWarehouse: Service<Collection<QuantityItem>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { QuantityItem.parse(it) } }
    }

    object ExecuteCommand {
        fun build(commandId: ID): Nothing  = TODO()
    }

    object ExecuteScript {
        fun build(script: Script): Nothing  = TODO()
    }

    object RemoveItem {
        fun build(item: WarehouseItem): Nothing  = TODO()
    }

    object RetrieveItem//: Service(TODO(), RETRIEVE_ITEM.id)

    object StoreItem {
        fun build(item: WarehouseItem): Nothing  = TODO()
    }
}