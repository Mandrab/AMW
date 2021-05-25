package common.ontology.dsl.abstraction

/**
 * Represents 'description' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Description {

    data class Description(val name: String)

    fun description(id: String) = Description(id)
}
