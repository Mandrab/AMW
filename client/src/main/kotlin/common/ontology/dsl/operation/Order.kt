package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email
import common.ontology.dsl.abstraction.Item.QuantityItem

object Order {

    data class Order(val client: Client, val email: Email, val address: Address, var items: List<QuantityItem>) {

        operator fun get(vararg items: QuantityItem) = get(items.toList())

        operator fun get(items: List<QuantityItem>) = apply { this.items = items }

        operator fun plusAssign(item: QuantityItem) { items += item }
    }

    fun order(client: Client, email: Email, address: Address) = Order(client, email, address, emptyList())
}