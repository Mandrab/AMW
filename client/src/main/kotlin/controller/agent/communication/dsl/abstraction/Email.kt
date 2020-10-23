package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Email(val address: String): Term {

    override fun term(): Literal = "email"(address)

    companion object {
        fun email(address: String) = Email(address)
    }
}