package controller.agent.communication.translation.out

import common.ontology.dsl.operation.Command.AddCommand
import common.ontology.dsl.operation.Item.AddItem
import common.ontology.dsl.operation.Command.ExecuteCommand
import common.ontology.dsl.operation.Order.PlaceOrder
import common.ontology.dsl.operation.Item.RemoveItem
import common.ontology.dsl.operation.Order.InfoOrders
import common.ontology.dsl.operation.Info
import controller.agent.communication.translation.out.AbstractionTerms.term
import controller.agent.communication.translation.out.Literals.get
import controller.agent.communication.translation.out.Literals.invoke
import controller.agent.communication.translation.out.Literals.toTerm
import jason.asSyntax.Literal

/**
 * Groups parsing functionalities for outgoing messages
 * It define the expected format for each outgoing message
 *
 * @author Paolo Baldini
 */
object OperationTerms {

    fun AddCommand.term(): Literal = "add"(command.term())

    fun AddItem.term(): Literal = "add"(item.term())

    fun ExecuteCommand.term(): Literal = "command"(id.term())

    fun PlaceOrder.term(): Literal = "order"(client.term(), email.term(), address.term())[items.map { it.term() }]

    fun InfoOrders.term(): Literal = "info"(client.term(), email.term())

    fun RemoveItem.term(): Literal = "remove"(item.term())

    fun Info.Info.term(): Literal = "info"(target.name.toLowerCase().toTerm())
}
