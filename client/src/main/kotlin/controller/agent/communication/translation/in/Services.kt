package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Command.CommandInfo
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.abstraction.Variant.Variant
import common.ontology.dsl.operation.Order.PlaceOrder
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.`in`.OperationTerms.parse
import jade.lang.acl.ACLMessage

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

    object InfoOrders: Service<Collection<PlaceOrder>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { PlaceOrder.parse(it) } }
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