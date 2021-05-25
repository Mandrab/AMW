package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem

/**
 * Represents 'item' operation(s) in the system
 * It refer to ontology abstractions
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Item {

    /**
     * Represents a request for item addition
     *
     * @author Paolo Baldini
     */
    data class AddItem(val item: WarehouseItem) { companion object }

    /**
     * Represents a request for item removal
     *
     * @author Paolo Baldini
     */
    data class RemoveItem(val item: QuantityItem) { companion object }

    fun add(item: WarehouseItem) = AddItem(item)

    fun remove(item: QuantityItem) = RemoveItem(item)
}
