package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Command
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.operation.Order.InfoOrder
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.`in`.OperationTerms.parse
import jade.lang.acl.ACLMessage

object Services {

    interface Service<T> {
        val parse: (message: ACLMessage) -> T
    }

    object InfoCommands: Service<Collection<Command.Command>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { Command.parse(it) } }
    }

    object InfoOrders: Service<Collection<InfoOrder>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { InfoOrder.parse(it) } }
    }

    object InfoWarehouse: Service<Collection<Product>> {
        override val parse = { message: ACLMessage -> message.content.asList().map { Product.parse(it) } }
    }
}
