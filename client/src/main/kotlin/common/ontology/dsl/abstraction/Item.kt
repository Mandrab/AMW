package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity

object Item {

    interface Item { val id: ID }

    data class Product(override val id: ID, var positions: Collection<Position>): Item {

        operator fun get(vararg positions: Position) = get(positions.toList())

        operator fun get(positions: Collection<Position>) = apply { this.positions = positions }

        operator fun plusAssign(position: Position) { positions += position }

        companion object
    }

    data class WarehouseItem(override val id: ID, val position: Position): Item { companion object }

    data class QuantityItem(override val id: ID, val quantity: Quantity): Item { companion object }

    fun item(id: ID, vararg positions: Position) = Product(id, positions.toList())

    fun item(id: ID, position: Position) = WarehouseItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)

    infix fun Item.sameId(item: Item): Boolean = id == item.id
}

