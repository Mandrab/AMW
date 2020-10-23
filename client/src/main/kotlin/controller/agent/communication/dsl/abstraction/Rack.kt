package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Rack(val id: Int): Term {

    override fun term(): Literal = "rack"(id)

    companion object {
        fun rack(id: Int) = Rack(id)
    }
}