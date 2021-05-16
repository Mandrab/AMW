package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.operation.Order.InfoOrder
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
        fun build(command: Command): Nothing  = TODO()
    }

    object InfoCommands//: Service(MANAGEMENT_COMMANDS.id, INFO_COMMANDS.id)

    object InfoOrders: Service<Collection<InfoOrder>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { InfoOrder.parse(it) } }
    }

    object InfoWarehouse: Service<Collection<Product>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { Product.parse(it) } }
    }

    object ExecuteCommand {
        fun build(commandId: ID): Nothing  = TODO()
    }

    object RemoveItem: Service<Unit> { override val parse = { _: ACLMessage -> Unit } }

    object StoreItem: Service<Unit> { override val parse = { _: ACLMessage -> Unit } }
}
