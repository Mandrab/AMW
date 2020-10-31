package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem

object Order {

    data class PlaceOrder(
        val client: Client,
        val email: Email,
        val address: Address,
        var items: Collection<QuantityItem> = emptyList()
    ) {
        operator fun get(vararg items: QuantityItem) = get(items.toList())

        operator fun get(items: Collection<QuantityItem>) = apply { this.items = items }

        operator fun plusAssign(item: QuantityItem) { items += item }

        companion object
    }

    data class InfoOrders(val client: Client, val email: Email) { companion object }

    fun order(client: Client, email: Email, address: Address) = PlaceOrder(client, email, address)

    fun info(client: Client, email: Email) = InfoOrders(client, email)
}