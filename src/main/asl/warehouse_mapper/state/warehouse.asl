/***********************************************************************************************************************
 Initial beliefs and rules. This file acts like a DB initializer
 Here, the initial known items and positions are specified
 **********************************************************************************************************************/

item(id("Item 1")) [
		position(rack(1), shelf(1), quantity(5)),
		position(rack(1), shelf(2), quantity(8)),
		position(rack(2), shelf(3), quantity(7))
].
item(id("Item 2")) [
		position(rack(2), shelf(4), quantity(1))
].
item(id("Item 3")) [
		position(rack(2), shelf(5), quantity(1))
].
item(id("Item 4")) [
        position(rack(2), shelf(6), quantity(3))
].
item(id("Item 5")) [
        position(rack(3), shelf(1), quantity(7)),
        position(rack(3), shelf(2), quantity(9))
].
item(id("Item 6")) [
        position(rack(3), shelf(3), quantity(17)),
        position(rack(3), shelf(4), quantity(19))
].
