package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal
import jason.asSyntax.StringTermImpl

data class OrderInfo(val client: String, val email: String): Term {

    override fun term(): Literal = "info"(StringTermImpl(client), StringTermImpl(email))

    companion object {
        fun info(client: String, email: String) = OrderInfo(client, email)
    }
}