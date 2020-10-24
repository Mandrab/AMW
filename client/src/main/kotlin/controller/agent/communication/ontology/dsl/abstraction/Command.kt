package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Command(val id: ID, val name: Name, val description: Description): Term {

    override fun term(): Literal = "command"(id.term(), name.term(), description.term())

    companion object {
        fun command(id: ID, name: Name, description: Description) = Command(id, name, description)
    }
}