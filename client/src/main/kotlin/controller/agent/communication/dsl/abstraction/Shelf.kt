package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Shelf(val id: Int): Term {

    override fun term(): Literal = "shelf"(id)

    companion object {
        fun shelf(id: Int) = Shelf(id)
    }
}