/***********************************************************************************************************************
 Initial beliefs and rules (warehouse state)
 **********************************************************************************************************************/

// initial known items and positions:
item(id("Item 1"), quantity(20), reserved(0)) [
		position(rack(5), shelf(3), quantity(5)),
		position(rack(5), shelf(2), quantity(8)),
		position(rack(6), shelf(3), quantity(7)) ].
item(id("Item 2"), quantity(1), reserved(1)) [
		position(rack(2), shelf(4), quantity(1)) ].
item(id("Item 3"), quantity(1), reserved(0)) [
		position(rack(2), shelf(5), quantity(1)) ].
item(id("Item 4"), quantity(3), reserved(0)) [
        position(rack(2), shelf(5), quantity(3)) ].