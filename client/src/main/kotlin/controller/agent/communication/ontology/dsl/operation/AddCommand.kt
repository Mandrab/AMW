package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.Command
import jason.asSyntax.Literal

data class AddCommand(val command: Command): Term {

    override fun term(): Literal = "add"(command.term())

    companion object {
        fun add(command: Command) = AddCommand(command)
    }
}