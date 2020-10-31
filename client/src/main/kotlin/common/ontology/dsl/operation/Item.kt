package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Item.WarehouseItem

object Item {

    data class AddItem(val item: WarehouseItem) { companion object }

    data class RemoveItem(val item: WarehouseItem) { companion object }

    fun add(item: WarehouseItem) = AddItem(item)

    fun remove(item: WarehouseItem) = RemoveItem(item)
}