package common.type

import org.junit.Test
import org.junit.Assert.*

/**
 * Test command class
 *
 * @author Paolo Baldini
 */
class TestItem {

	@Test fun testClone() {
		var item = Item("id", 5, emptyList())
		assertFalse(item === item.clone())
		assertEquals(item, item.clone())
		assertEquals(item.hashCode(), item.clone().hashCode())

		assertNotEquals(Command("", "", "", emptyList()), item)
		assertNotEquals(Command("", "", "", emptyList()).hashCode(), item.hashCode())

		val positions = listOf(Triple(0, 0, 0), Triple(1, 1, 1), Triple(2, 2, 2))
		item = Item("id", 3, positions)
		assertFalse(item === item.clone())
		assertEquals(item, item.clone())
		assertEquals(item.hashCode(), item.clone().hashCode())

		assertNotEquals(Item("id", 3, emptyList()), item)
		assertNotEquals(Item("id", 3, emptyList()).hashCode(), item.hashCode())
	}

	@Test fun testParse() {
		var itemStr = "item(id(\"Item 1\"), reserved(0)) [position(rack(5), shelf(3), quantity(10))]"
		var item = Item("\"Item 1\"", 0, listOf(Triple(5, 3, 10)))
		assertEquals(item, Item.parse(itemStr))

		itemStr = "item(id(\"Item 1\"), reserved(0)) [position(rack(5), shelf(3), quantity(5))," +
				"position(rack(5), shelf(2), quantity(8)), position(rack(6), shelf(3), quantity(7))]"
		item = Item("\"Item 1\"", 0, listOf(Triple(5, 3, 5), Triple(5, 2, 8), Triple(6, 3, 7)))
		assertEquals(item, Item.parse(itemStr))
	}
}