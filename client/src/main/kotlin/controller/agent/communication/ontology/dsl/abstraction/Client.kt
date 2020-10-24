package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Client(val name: String): Term {

    override fun term(): Literal = "client"(name)

    companion object {
        fun client(name: String) = Client(name)
    }
}