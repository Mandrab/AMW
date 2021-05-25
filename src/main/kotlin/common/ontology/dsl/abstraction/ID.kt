package common.ontology.dsl.abstraction

/**
 * Represents 'id' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object ID {

    data class ID(val name: String)

    fun id(id: String) = ID(id)
}
