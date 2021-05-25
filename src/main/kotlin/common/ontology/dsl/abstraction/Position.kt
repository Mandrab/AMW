package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Quantity.Quantity
import common.ontology.dsl.abstraction.Rack.Rack
import common.ontology.dsl.abstraction.Shelf.Shelf

/**
 * Represents 'position' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Position {

    data class Position(val rack: Rack, val shelf: Shelf, val quantity: Quantity)

    fun position(rack: Rack, shelf: Shelf, quantity: Quantity) = Position(rack, shelf, quantity)
}
