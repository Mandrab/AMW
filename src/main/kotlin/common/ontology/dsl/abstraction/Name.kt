package common.ontology.dsl.abstraction

/**
 * Represents 'name' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Name {

    data class Name(val name: String)

    fun name(name: String) = Name(name)
}
