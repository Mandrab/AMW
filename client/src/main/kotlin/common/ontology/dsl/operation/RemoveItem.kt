package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Item.WarehouseItem

object RemoveItem {

    data class RemoveItem(val item: WarehouseItem)

    fun remove(item: WarehouseItem) = RemoveItem(item)
}