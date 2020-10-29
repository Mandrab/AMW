package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity

object Item {

    interface Item { val id: ID }

    data class Product(override val id: ID, val reserved: Int, val positions: List<Position>): Item

    data class WarehouseItem(override val id: ID, val position: Position): Item

    data class QuantityItem(override val id: ID, val quantity: Quantity): Item

    fun item(id: ID, reserved: Int, positions: List<Position>) = Product(id, reserved, positions)

    fun item(id: ID, position: Position) = WarehouseItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)

    infix fun Item.sameId(item: Item): Boolean = id == item.id
}

