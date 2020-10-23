package controller.agent.communication

import controller.agent.communication.LiteralBuilder.Companion.pairTerm
import jason.asSyntax.StringTermImpl
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test literal builder
 *
 * @author Paolo Baldini
 */
class TestLiteralBuilder {

	@Test fun testBasicStringBuild() {
		assertEquals("ciao", LiteralBuilder("ciao").build().toString())
		assertEquals("ciao()", LiteralBuilder("ciao").setValues("").build().toString())
		assertEquals("ciao(str1,str2)", LiteralBuilder("ciao").setValues("str1", "str2").build().toString())
		assertEquals("ciao[]", LiteralBuilder("ciao").setQueue("").build().toString())
		assertEquals("ciao[str1,str2]", LiteralBuilder("ciao").setQueue("str1", "str2").build().toString())
	}

	@Test fun testBasicTermBuild() {
		assertEquals("ciao", LiteralBuilder("ciao").build().toString())
		assertEquals("ciao(\"\")", LiteralBuilder("ciao").setValues(StringTermImpl("")).build().toString())
		assertEquals("ciao(\"str1\",\"str2\")", LiteralBuilder("ciao")
				.setValues(StringTermImpl("str1"), StringTermImpl("str2")).build().toString())
		assertEquals("ciao[\"\"]", LiteralBuilder("ciao").setQueue(StringTermImpl("")).build().toString())
		assertEquals("ciao[\"str1\",\"str2\"]", LiteralBuilder("ciao")
				.setQueue(StringTermImpl("str1"), StringTermImpl("str2")).build().toString())
	}

	@Test fun testNestedBuild() {
		assertEquals("ciao(ciao2(str1))", LiteralBuilder("ciao").setValues(LiteralBuilder("ciao2").setValues("str1")
				.build()).build().toString())
		assertEquals("ciao[ciao2(str1)]", LiteralBuilder("ciao").setQueue(LiteralBuilder("ciao2").setValues("str1")
				.build()).build().toString())
		assertEquals("ciao(ciao2[str1])", LiteralBuilder("ciao").setValues(LiteralBuilder("ciao2").setQueue("str1")
			.build()).build().toString())
		assertEquals("ciao[ciao2[str1]]", LiteralBuilder("ciao").setQueue(LiteralBuilder("ciao2").setQueue("str1")
			.build()).build().toString())
	}

	@Test fun testPairTerm() {
		assertEquals("str1(str2)", pairTerm("str1", "str2").toString())
		assertEquals("str1(100.001)", pairTerm("str1", 100.001).toString())
	}
}