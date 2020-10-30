package controller.agent.communication.translation.out

import common.ontology.dsl.operation.AddCommand.AddCommand
import common.ontology.dsl.operation.AddItem.AddItem
import common.ontology.dsl.operation.AddVersion.AddVersion
import common.ontology.dsl.operation.Execute.ExecuteScript
import common.ontology.dsl.operation.Execute.ExecuteCommand
import common.ontology.dsl.operation.Order.Order
import common.ontology.dsl.operation.Order.OrderInfo
import common.ontology.dsl.operation.RemoveItem.RemoveItem
import common.ontology.dsl.operation.RetrieveOrder.RetrieveOrder
import controller.agent.communication.translation.out.Literals.get
import controller.agent.communication.translation.out.Literals.invoke
import controller.agent.communication.translation.out.Literals.toTerm
import controller.agent.communication.translation.out.AbstractionTerms.term
import jason.asSyntax.Literal

object OperationTerms {

    fun AddCommand.term(): Literal = "add"(command.term())

    fun AddItem.term(): Literal = "add"(item.term())

    fun AddVersion.term(): Literal = "add"(commandId.toTerm(), variant.term())

    fun ExecuteCommand.term(): Literal = "execute"(commandId.term("command_id"))

    fun ExecuteScript.term(): Literal = "execute"(script.term())

    fun Order.term(): Literal = "order"(client.term(), email.term(), address.term())
            .get(*items.map { it.term() }.toTypedArray())

    fun OrderInfo.term(): Literal = "info"(client.term(), email.term())

    fun RemoveItem.term(): Literal = "remove"(item.term())

    fun RetrieveOrder.term(): Literal = "retrieve"(orderId.term("order_id"))
            .get(*items.map { it.term() }.toTypedArray())
}