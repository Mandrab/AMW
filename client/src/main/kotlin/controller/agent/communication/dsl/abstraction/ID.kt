package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class ID(val name: String, val syntax: String): Term {

    override fun term(): Literal = term(syntax)

    fun term(syntax: String): Literal = syntax(name)

    companion object {
        fun id(id: String) = ID(id, "id")

        fun v_id(id: String) = ID(id, "v_id")

        fun order_id(id: String) = ID(id, "order_id")

        fun command_id(id: String) = ID(id, "command_id")
    }
}