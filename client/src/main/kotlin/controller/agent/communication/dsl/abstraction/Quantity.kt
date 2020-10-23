package controller.agent.communication.dsl.abstraction

import controller.agent.communication.Literals.toTerm
import controller.agent.communication.dsl.Term

data class Quantity(val value: Int): Term {

    override fun term() = value.toTerm()

    companion object {
        fun quantity(value: Int) = Quantity(value)
    }
}