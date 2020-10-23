package controller.agent.communication

import jason.asSyntax.*

/**
 * An utility class created to easily build literals
 *
 * @author Paolo Baldini
 */
class LiteralBuilder(name: String) {
	companion object {
		fun pairTerm(name: String, value: String) = Structure(name).apply { addTerm(Atom(value)) }
		fun pairTerm(name: String, value: Double) = Structure(name).apply { addTerm(NumberTermImpl(value)) }
	}

	private val name = Atom(name)
	private val values by lazy { mutableListOf<Term>().apply { isValuesInitialized = true } }
	private var isValuesInitialized: Boolean = false
	private val queue by lazy { mutableListOf<Term>().apply { isQueueInitialized = true } }
	private var isQueueInitialized: Boolean = false

	/**
	 * Set values of the literal. A value is considered as:
	 *      name(VALUE, VALUE, ...)
	 */
	fun setValues(vararg l: String) = apply { values.addAll(l.map { Atom(it) }) }       // add all string

	/**
	 * Set values of the literal. A value is considered as:
	 *      name(VALUE, VALUE, ...)
	 */
	fun setValues(vararg l: Term) = apply { values.addAll(l) }                          // add all string

	/**
	 * Set queue of the literal. A queue-element is considered as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	fun setQueue(vararg l:  String) = apply { queue.addAll(l.map { Atom(it) }) }        // add all string

	/**
	 * Set queue of the literal. A queue-element is considered as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	fun setQueue(vararg l: Term) = apply { queue.addAll(l) }                            // add all string

	/**
	 * Assemble the literal
	 */
	fun build(): Literal {
		val literal = LiteralImpl(name)

		if (isValuesInitialized && values.isEmpty()) literal.addTerm(Atom(""))
		if (isQueueInitialized && queue.isEmpty()) literal.addAnnot(Atom(""))

		literal.addTerms(values)
		literal.addAnnots(queue)

		return literal
	}
}