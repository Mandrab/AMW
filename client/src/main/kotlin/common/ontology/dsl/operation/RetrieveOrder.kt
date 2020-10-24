package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem

object RetrieveOrder {

    data class RetrieveOrder(val orderId: ID, var items: List<QuantityItem> = emptyList()) {

        operator fun get(vararg items: QuantityItem) = get(items.toList())

        operator fun get(items: List<QuantityItem>) = apply { this.items = items }

        operator fun plusAssign(item: QuantityItem) { items += item }
    }

    fun retrieve(orderId: ID, vararg items: QuantityItem) = RetrieveOrder(orderId, items.toList())
}