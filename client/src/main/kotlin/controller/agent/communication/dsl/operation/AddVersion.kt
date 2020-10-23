package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.invoke
import controller.agent.communication.Literals.toTerm
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.Variant
import jason.asSyntax.Literal

data class AddVersion(val commandId: String, val variant: Variant): Term {

    override fun term(): Literal = "add"(commandId.toTerm(), variant.term())

    companion object {
        fun add(commandId: String, variant: Variant) = AddVersion(commandId, variant)
    }
}