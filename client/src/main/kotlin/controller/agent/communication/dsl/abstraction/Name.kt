package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Name(val name: String): Term {

    override fun term(): Literal = "name"(name)

    companion object {
        fun name(id: String) = Name(id)
    }
}