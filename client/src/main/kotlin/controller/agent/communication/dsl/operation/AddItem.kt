package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.Item.StoredItem
import jason.asSyntax.Literal

data class AddItem(val item: StoredItem): Term {

    override fun term(): Literal = "add"(item.term())

    companion object {
        fun add(item: StoredItem) = AddItem(item)
    }
}