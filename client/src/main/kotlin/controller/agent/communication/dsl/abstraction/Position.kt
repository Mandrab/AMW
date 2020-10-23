package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Position(val rack: Rack, val shelf: Shelf, val quantity: Quantity): Term {

    override fun term(): Literal = "position"(rack.term(), shelf.term(), quantity.term())

    companion object {
        fun position(rack: Rack, shelf: Shelf, quantity: Quantity) = Position(rack, shelf, quantity)
    }
}