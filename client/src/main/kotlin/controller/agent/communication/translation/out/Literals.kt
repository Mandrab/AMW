package controller.agent.communication.translation.out

import jason.asSyntax.*

/**
 * An utility class created to easily build literals.
 * A simil-DSL is offered for this task.
 *
 * @author Paolo Baldini
 */
object Literals {

	/**
	 * Set string(s) as 'value(s)' of the literal. A value is meant as:
	 *      name(VALUE, VALUE, ...)
	 */
	operator fun String.invoke(first: String, vararg others: String) = invoke(listOf(first,*others).map { it.toTerm() })

	/**
	 * Set number(s) as 'value(s)' of the literal. A value is meant as:
	 *      name(VALUE, VALUE, ...)
	 */
	operator fun String.invoke(first: Number, vararg others: Number) = invoke(listOf(first,*others).map { it.toTerm() })

	/**
	 * Set term(s) as 'value(s)' of the literal. A value is meant as:
	 *      name(VALUE, VALUE, ...)
	 */
	operator fun String.invoke(vararg terms: Term) = invoke(terms.toList())

	/**
	 * Set term list as 'value(s)' of the literal. A value is meant as:
	 *      name(VALUE, VALUE, ...)
	 */
	operator fun String.invoke(terms: List<Term>) = LiteralImpl(this)
			.addTerms(terms.ifEmpty { listOf("".toTerm()) })

	/**
	 * Set string(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun String.get(vararg strings: String) = LiteralImpl(Atom(this)).get(*strings)

	/**
	 * Set number(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun String.get(vararg numbers: Number) = LiteralImpl(Atom(this)).get(*numbers)

	/**
	 * Set term(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun String.get(vararg terms: Term) = LiteralImpl(Atom(this)).get(*terms)

	/**
	 * Converts a string to a term
	 */
	fun String.toTerm() = Atom(this)

	/**
	 * Converts a string to a string-term
	 */
	fun String.toStringTerm() = StringTermImpl(this)

	/**
	 * Set string(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun Literal.get(vararg strings: String) = get(strings.map { it.toTerm() })

	/**
	 * Set number(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun Literal.get(vararg numbers: Number) = get(numbers.map { it.toTerm() })

	/**
	 * Set term(s) as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun Literal.get(vararg terms: Term) = get(terms.toList())

	/**
	 * Set terms' list as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	operator fun Literal.get(terms: List<Term>) = addAnnots(terms.ifEmpty { listOf("".toTerm()) })

	/**
	 * Set terms' list as 'queue' of the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	fun Literal.queue(terms: List<Term>) = get(terms)

	/**
	 * Create and empty 'queue' for the literal. A queue is meant as:
	 *      name[QUEUE-ELEMENT, QUEUE-ELEMENT, ...]
	 */
	fun Literal.emptyQueue() = get(emptyList())

	/**
	 * Converts a number to a term
	 */
	fun Number.toTerm() = NumberTermImpl(this.toDouble())
}
