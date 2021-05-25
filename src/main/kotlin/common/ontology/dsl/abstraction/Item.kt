package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity

/**
 * Represents 'item' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Item {

    interface Item { val id: ID }

    /**
     * Item who is associated with multiple positions
     * Used for warehouse information
     *
     * @author Paolo Baldini
     */
    data class Product(override val id: ID, var positions: Collection<Position>): Item {

        operator fun get(vararg positions: Position) = get(positions.toList())

        operator fun get(positions: Collection<Position>) = apply { this.positions = positions }

        operator fun plusAssign(position: Position) { positions += position }

        companion object
    }

    /**
     * Item who is associated with only one position
     * Used for warehouse additions
     *
     * @author Paolo Baldini
     */
    data class WarehouseItem(override val id: ID, val position: Position): Item { companion object }

    /**
     * Item who is associated with a quantity
     * Used for order placement
     *
     * @author Paolo Baldini
     */
    data class QuantityItem(override val id: ID, val quantity: Quantity): Item { companion object }

    fun item(id: ID, vararg positions: Position) = Product(id, positions.toList())

    fun item(id: ID, position: Position) = WarehouseItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)

    infix fun Item.sameId(item: Item): Boolean = id == item.id
}
