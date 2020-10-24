package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity

object Item {

    data class WarehouseItem(val id: ID, val position: Position)

    data class QuantityItem(val id: ID, val quantity: Quantity)

    fun item(id: ID, position: Position) = WarehouseItem(id, position)

    fun item(id: ID, quantity: Quantity) = QuantityItem(id, quantity)
}

