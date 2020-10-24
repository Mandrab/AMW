package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Address(val address: String): Term {

    override fun term(): Literal = "address"(address)

    companion object {
        fun address(address: String) = Address(address)
    }
}