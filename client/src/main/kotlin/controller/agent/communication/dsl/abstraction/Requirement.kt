package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.toTerm
import controller.agent.communication.dsl.Term

data class Requirement(val name: String): Term {

    override fun term() = name.toTerm()

    companion object {
        fun requirement(name: String) = Requirement(name)
    }
}