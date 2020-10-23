package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.get
import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.Address
import controller.agent.communication.dsl.abstraction.Client
import controller.agent.communication.dsl.abstraction.Email
import controller.agent.communication.dsl.abstraction.Item.QuantityItem
import jason.asSyntax.Literal

data class Order(val client: Client, val email: Email, val address: Address, var items: List<QuantityItem>): Term {

    override fun term(): Literal = "order"(client.term(), email.term(), address.term())
            .get(*items.map { it.term() }.toTypedArray())

    operator fun get(vararg items: QuantityItem) = get(items.toList())

    operator fun get(items: List<QuantityItem>) = apply { this.items = items }

    operator fun plusAssign(item: QuantityItem) { items += item }

    companion object {
        fun order(client: Client, email: Email, address: Address) = Order(client, email, address, emptyList())
    }
}