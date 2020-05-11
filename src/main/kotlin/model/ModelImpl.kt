package model

import common.type.Command
import common.type.Item

/**
 * An implementations of @see Model interface. See it for more information
 *
 * @author Paolo Baldini
 */
class ModelImpl: Model {
	/** {@inheritDoc} */
	override var commands: Set<Command> = emptySet()

	/** {@inheritDoc} */
	override var items: Set<Item> = emptySet()

	/** {@inheritDoc} */
	override val orders: Set<Order> = mutableSetOf()

	/** {@inheritDoc} */
	override fun addOrder(order: Order) = (orders as MutableSet).add(order)
}