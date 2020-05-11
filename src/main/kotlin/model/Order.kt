package model

import common.type.Item

/**
 * Represents an order made by an user. This class only contains data
 *
 * @author Paolo Baldini
 */
data class Order(val id: String, var status: Status, val items: List<Item>) {
	/**
	 * Represents the status of the order
	 *
	 * @author Paolo Baldini
	 */
	enum class Status {
		SUBMITTED,
		ACCEPTED,
		REFUSED,
		COMPLETED
	}

	init {
		check(items.isNotEmpty()) { "An order must contains items!" }
	}

	/**
	 * {@inheritDoc}
	 */
	override fun equals(other: Any?) = other is Order && other.id == id

	/**
	 * {@inheritDoc}
	 */
	override fun hashCode() = id.hashCode()
}