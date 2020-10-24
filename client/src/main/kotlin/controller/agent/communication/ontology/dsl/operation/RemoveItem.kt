package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.Item.StoredItem
import jason.asSyntax.Literal

data class RemoveItem(val item: StoredItem): Term {

    override fun term(): Literal = "remove"(item.term())

    companion object {
        fun remove(item: StoredItem) = RemoveItem(item)
    }
}