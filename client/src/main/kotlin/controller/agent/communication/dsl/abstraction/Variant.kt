package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.Literals.get
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Variant(val id: ID, val script: Script, val requirements: List<Requirement>): Term {

    override fun term(): Literal = "variant"(id.term("v_id"),
            "requirements".get(*requirements.map { it.term() }.toTypedArray()), script.term())

    companion object {
        fun variant(id: ID, script: Script, requirements: List<Requirement>) = Variant(id, script, requirements)
    }
}