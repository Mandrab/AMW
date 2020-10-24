package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.invoke
import controller.agent.communication.Literals.get
import controller.agent.communication.dsl.Term
import jason.asSyntax.Literal

data class Script(val script: String, private var requirements: List<Requirement>? = null): Term {

    override fun term(): Literal = "script"(script).run { requirements?.let { this[it.map { r -> r.term() }] } ?: this }

    operator fun get(vararg requirements: Requirement) = get(requirements.toList())

    operator fun get(requirements: List<Requirement>) = apply { this.requirements = requirements }

    operator fun plusAssign(requirement: Requirement) {
        requirements = requirements?.let { it + requirement } ?: listOf(requirement)
    }

    companion object {
        fun script(script: String) = Script(script)
    }
}