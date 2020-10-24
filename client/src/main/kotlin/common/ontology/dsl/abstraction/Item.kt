package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity

object Item {

    data class Product(val id: ID, val reserved: Int, val positions: List<Position>)

    data class WarehouseItem(val id: ID, val position: Position)

    data class QuantityItem(val id: ID, val quantity: Quantity)

    fun item(id: ID, reserved: Int, positions: List<Position>) = Product(id, reserved, positions)

    fun item(id: ID, position: Position) = WarehouseItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)
}

