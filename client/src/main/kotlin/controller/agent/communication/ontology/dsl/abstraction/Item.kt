package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

object Item {

    data class StoredItem(val id: ID, val position: Position): Term {

        override fun term(): Literal = "item"(id.term(), position.term())
    }

    data class QuantityItem(val id: ID, val quantity: Quantity): Term {

        override fun term(): Literal = "item"(id.term(), quantity.term())
    }

    fun item(id: ID, position: Position) = StoredItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)
}

