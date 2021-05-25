package common.ontology.dsl.abstraction

/**
 * Represents 'address' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Address {

    data class Address(val address: String)

    fun address(address: String) = Address(address)
}
