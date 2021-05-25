package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Status.Status

/**
 * Represents 'order' operation(s) in the system
 * It refer to ontology abstractions
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Order {

    /**
     * Represents a request for order placement
     *
     * @author Paolo Baldini
     */
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

    /**
     * Represents a response for orders information
     *
     * @author Paolo Baldini
     */
    data class InfoOrder(val id: ID, val status: Status) { companion object }

    /**
     * Represents a request for orders information
     *
     * @author Paolo Baldini
     */
    data class InfoOrders(val client: Client, val email: Email) { companion object }

    fun order(client: Client, email: Email, address: Address) = PlaceOrder(client, email, address)

    fun info(id: ID, status: Status) = InfoOrder(id, status)

    fun info(client: Client, email: Email) = InfoOrders(client, email)
}
