package common.ontology.dsl.abstraction

/**
 * Represents 'rack' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Rack {

    data class Rack(val id: Int)

    fun rack(id: Int) = Rack(id)
}
