package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Description(val name: String): Term {

    override fun term(): Literal = "description"(name)

    companion object {
        fun description(id: String) = Description(id)
    }
}