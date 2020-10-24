package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Item.WarehouseItem

object AddItem {

    data class AddItem(val item: WarehouseItem)

    fun add(item: WarehouseItem) = AddItem(item)
}