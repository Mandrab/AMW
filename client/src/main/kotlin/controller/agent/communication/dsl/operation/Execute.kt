package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.ID
import controller.agent.communication.dsl.abstraction.Script
import jason.asSyntax.Literal

object Execute {

    data class ExecuteCommand(val commandId: ID) : Term {

        override fun term(): Literal = "execute"(commandId.term("command_id"))
    }

    data class ExecuteScript(val script: Script) : Term {

        override fun term(): Literal = "execute"(script.term())
    }

    fun execute(script: Script) = ExecuteScript(script)

    fun execute(commandId: ID) = ExecuteCommand(commandId)
}