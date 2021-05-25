package common.ontology.dsl.abstraction

/**
 * Represents 'shelf' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Shelf {

    data class Shelf(val id: Int)

    fun shelf(id: Int) = Shelf(id)
}
