package common.ontology.dsl.abstraction

/**
 * Represents 'email' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Email {

    data class Email(val address: String)

    fun email(address: String) = Email(address)
}
