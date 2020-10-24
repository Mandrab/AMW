package controller.agent.communication.dsl.operation

import controller.agent.communication.Literals.get
import controller.agent.communication.Literals.invoke
import controller.agent.communication.dsl.Term
import controller.agent.communication.dsl.abstraction.ID
import controller.agent.communication.dsl.abstraction.Item.QuantityItem
import jason.asSyntax.Literal

data class RetrieveOrder(val orderId: ID, private var items: List<QuantityItem> = emptyList()): Term {

    override fun term(): Literal = "retrieve"(orderId.term("order_id"))
            .get(*items.map { it.term() }.toTypedArray())

    operator fun get(vararg items: QuantityItem) = get(items.toList())

    operator fun get(items: List<QuantityItem>) = apply { this.items = items }

    operator fun plusAssign(item: QuantityItem) { items += item }

    companion object {
        fun retrieve(orderId: ID, vararg items: QuantityItem) = RetrieveOrder(orderId, items.toList())
    }
}