package common.ontology.dsl.abstraction

/**
 * Represents 'quantity' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Quantity {

    data class Quantity(val value: Int)

    fun quantity(value: Int) = Quantity(value)
}
