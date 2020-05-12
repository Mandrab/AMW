package model

import common.type.Command
import common.type.Item

/**
 * Interface to the application model. It aims to store local information
 *
 * @author Paolo Baldini
 */
interface Model {
	/**
	 * Contains the local copy of commands (or a subset of them)
	 */
	var commands: Set<Command>

	/**
	 * Contains the local copy of items (or a subset of them)
	 */
	var items: Set<Item>

	/**
	 * Contains the local copy of user orders (or a subset of them)
	 */
	val orders: Set<Order>

	/**
	 * Allow to add an order to the set.
	 *
	 * @return
	 *      true if addition succeed,
	 *      false otherwise
	 */
	fun addOrder(order: Order): Boolean
}