package model

import common.type.Command
import common.type.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TestModel {

	@Test fun testOrderAddition() {
		val model: Model = ModelImpl()

		assert(model.orders.isEmpty())

		var order = Order("id1", Order.Status.SUBMITTED, listOf(Item("", 0, emptyList())))
		assert(model.addOrder(order))
		assertEquals(1, model.orders.size)

		assertFalse(model.addOrder(order))
		assertEquals(1, model.orders.size)

		order = Order("id2", Order.Status.SUBMITTED, listOf(Item("", 0, emptyList())))
		assert(model.addOrder(order))
		assertEquals(2, model.orders.size)

		assertFalse(model.addOrder(order))
		assertEquals(2, model.orders.size)

		assert(model.commands.isEmpty())
		assert(model.items.isEmpty())
	}

	@Test fun testCommandsSet() {
		val model: Model = ModelImpl()

		assert(model.commands.isEmpty())

		val command = Command("id1", "name", "description", listOf())
		model.commands = setOf(command)
		assertEquals(1, model.commands.size)

		val commands = model.commands
		model.commands = setOf(command)
		assertFalse(commands === model.commands)
		assertEquals(1, model.commands.size)

		model.commands = emptySet()
		assert(model.commands.isEmpty())
		assert(model.orders.isEmpty())
		assert(model.items.isEmpty())
	}

	@Test fun testItemsSet() {
		val model: Model = ModelImpl()

		assert(model.items.isEmpty())

		val item = Item("id1", 0, listOf())
		model.items = setOf(item)
		assertEquals(1, model.items.size)

		val items = model.items
		model.items = setOf(item)
		assertFalse(items === model.items)
		assertEquals(1, model.items.size)

		model.items = emptySet()
		assert(model.items.isEmpty())
		assert(model.orders.isEmpty())
		assert(model.commands.isEmpty())
	}
}