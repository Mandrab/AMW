package controller.agent.communication.dsl

import jason.asSyntax.Term

interface Term {

    /**
     * Converts the element into a jason-term
     */
    fun term(): Term
}