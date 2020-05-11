package common.translation

import common.translation.LiteralParser.getValue
import common.translation.LiteralParser.split
import common.translation.LiteralParser.splitStructAndList
import org.junit.Assert.*
import org.junit.Test

class TestLiteralParser {
	@Test fun testSplit() {
		listOf(
			Pair("", arrayOf("")), Pair("[]", arrayOf("")),
			Pair("str1(ciao)", arrayOf("str1(ciao)")),
			Pair("str1(str2(ciao2), str3(ciao3))", arrayOf("str1(str2(ciao2), str3(ciao3))")),
			Pair("str1(ciao1), str2(ciao2)", arrayOf("str1(ciao1)", "str2(ciao2)")),
			Pair("str1(ciao1), str2(ciao2), str3(ciao3)", arrayOf("str1(ciao1)", "str2(ciao2)", "str3(ciao3)")),
			Pair("[str1(ciao)]", arrayOf("str1(ciao)")),
			Pair("[str1(str2(ciao2), str3(ciao3))]", arrayOf("str1(str2(ciao2), str3(ciao3))")),
			Pair("[str1(ciao1), str2(ciao2)]", arrayOf("str1(ciao1)", "str2(ciao2)")),
			Pair("[str1(ciao1), str2(ciao2), str3(ciao3)]", arrayOf("str1(ciao1)", "str2(ciao2)", "str3(ciao3)")),
			Pair("[lit(1)[lit(2),lit(3)], lit(2)[lit(3),lit(4)]]",
				arrayOf("lit(1)[lit(2),lit(3)]", "lit(2)[lit(3),lit(4)]"))
		).onEach { assertArrayEquals(it.second, split(it.first).toTypedArray()) }
	}

	@Test fun testSplitStructAndList() {
		listOf(
			Pair("", Pair("", "")),
			Pair("str1(ciao)", Pair("str1(ciao)", "")),
			Pair("str1(ciao)[]", Pair("str1(ciao)", "")),
			Pair("str1(ciao)[str2(ciao2)]", Pair("str1(ciao)", "str2(ciao2)")),
			Pair(
				"str1(ciao)[str2(ciao2), str3(ciao3)]",
				Pair("str1(ciao)", "str2(ciao2), str3(ciao3)")
			),
			Pair("str1(str2(ciao2)[str3(ciao3)])", Pair("str1(str2(ciao2)[str3(ciao3)])", "")),
			Pair(
				"str1(str2(ciao2)[str3(ciao3)[str4(ciao4)]])",
				Pair("str1(str2(ciao2)[str3(ciao3)[str4(ciao4)]])", "")
			)
		).onEach { assertEquals(it.second, splitStructAndList(it.first)) }
	}

	@Test fun testGetValue() {
		val inOut = listOf(
			Pair("str1(ciao)", "ciao"),
			Pair("str1(ciao)[]", "ciao"),
			Pair("str1(ciao)[str2(ciao2)]", "ciao"),
			Pair("str1(str2(ciao2))", "str2(ciao2)"),
			Pair("str1(str2(ciao2), str3(ciao3))", "str2(ciao2), str3(ciao3)")
		)
		testException(IllegalStateException::class.java, "should have thrown a IllegalStateException")
				{ getValue("") }
		testException(IllegalStateException::class.java, "should have thrown a IllegalStateException")
				{ getValue("(") }
		testException(IllegalStateException::class.java, "should have thrown a IllegalStateException")
				{ getValue("[(") }
		inOut.onEach { testException(null, "shouldn't throw Exception") {
			assertEquals(it.first, it.second, getValue(it.first)) }
		}
	}

	@Test fun testGetValueOf() {
		listOf(
			Triple("str1(ciao)", "str1", "ciao"),
			Triple("str1(ciao)[]", "str1", "ciao"),
			Triple("str1(ciao)[str2(ciao2)]", "str1", "ciao"),
			Triple("str1(str2(ciao2))", "str1", "str2(ciao2)"),
			Triple("str1(str2(ciao2), str3(ciao3))", "str1", "str2(ciao2), str3(ciao3)"),
			Triple("str1(ciao)[str2(ciao2)]", "str2", "ciao2"),
			Triple("str1(ciao)[str2(ciao2), str3(ciao3)]", "str3", "ciao3"),
			Triple("str1(str2(ciao2))", "str2", "ciao2"),
			Triple("str1(str2(ciao2), str3(ciao3))", "str3", "ciao3"),
			Triple("[str1(ciao), str2(ciao2), str3(ciao3)]", "str2", "ciao2")
		).onEach { assertEquals(it.third, getValue(it.first, it.second)) }
	}

	private fun testException(type: Class<*>?, msg: String, action: () -> Any) {
		try {
			action()
		} catch (e: Exception) {
			if (e::class.java != type) fail(msg)
		}
	}
}