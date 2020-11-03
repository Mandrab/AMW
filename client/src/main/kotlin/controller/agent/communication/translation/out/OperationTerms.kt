package controller.agent.communication.translation.out

import common.ontology.dsl.operation.Command.AddCommand
import common.ontology.dsl.operation.Item.AddItem
import common.ontology.dsl.operation.Version.AddVersion
import common.ontology.dsl.operation.Script.ExecuteScript
import common.ontology.dsl.operation.Command.ExecuteCommand
import common.ontology.dsl.operation.Order.PlaceOrder
import common.ontology.dsl.operation.Item.RemoveItem
import common.ontology.dsl.operation.Order.InfoOrders
import common.ontology.dsl.operation.Warehouse
import controller.agent.communication.translation.out.AbstractionTerms.term
import controller.agent.communication.translation.out.Literals.get
import controller.agent.communication.translation.out.Literals.invoke
import controller.agent.communication.translation.out.Literals.toTerm
import jason.asSyntax.Literal

object OperationTerms {

    fun AddCommand.term(): Literal = "add"(command.term())

    fun AddItem.term(): Literal = "add"(item.term())

    fun AddVersion.term(): Literal = "add"(commandId.toTerm(), variant.term())

    fun ExecuteCommand.term(): Literal = "execute"(commandId.term("command_id"))

    fun ExecuteScript.term(): Literal = "execute"(script.term())

    fun PlaceOrder.term(): Literal = "order"(client.term(), email.term(), address.term())[items.map { it.term() }].apply { println(this) }

    fun InfoOrders.term(): Literal = "info"(client.term(), email.term())

    fun RemoveItem.term(): Literal = "remove"(item.term())

    fun Warehouse.Info.term(): Literal = "info"(target.name.toLowerCase().toTerm())
}