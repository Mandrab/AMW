package asl.agent

import asl.agent.fake.FakeAdminAgent
import asl.agent.fake.FakeAminProxy
import com.google.common.io.Resources
import common.type.Command
import controller.agent.AgentUtils
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import util.ResultLock
import java.util.function.Function

class TestCommandManager {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestCommandManager.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()

		var expectedCommandsN = 2
		var expectedVariantsN = 3
	}

	@Test fun commandsLoading() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		val evaluated = ResultLock(false)

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.subscribeCommands(inlineOnNextObserver { c ->
			assertEquals(expectedCommandsN, c.groupBy { it.id }.count())
			assertEquals(expectedVariantsN, c.count())
			evaluated.tryComplete { true }
		})

		evaluated.maxTimeToComplete(3000)

		assert(evaluated.result)
	}

	@Test fun commandRequest() {
		var agent: FakeAdminAgent? = null
		val adminProxy = FakeAminProxy { agent = it as FakeAdminAgent }
		AgentUtils.startAgent(FakeAdminAgent::class.java, adminProxy)

		val result = ResultLock("")

		while (!adminProxy.isAvailable() || agent == null) Thread.sleep(50)

		agent!!.getCommand("Command1", Function { result.tryComplete { it!!.content } })

		val expectedBase = """command("Command1")[variant(v_id("vid0.0.0.1"),requirements["move"],""" +
				"""script("[  {@l1 +!main <- .println('Executing script ...');.wait(500); !b}, """ +
				"""{@l2 +!b <- .println('Script executed') }]"))"""
		val otherTestExecuted = """,variant(v_id("vid0.0.0.2"),requirements["req1","req2"],script("script2"))]"""

		result.maxTimeToComplete(1000)

		assert("$expectedBase]" == result.result || (expectedBase + otherTestExecuted) == result.result) { result.result }
	}

	@Test fun newCommandAddition() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		val evaluated1 = ResultLock(false)
		val evaluated2 = ResultLock(false)

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.add(Command("Command1", "nameX", "descriptionX", listOf(
			Command.Version("idX", listOf("req1", "req2"), "script1"),
			Command.Version("idXX", listOf("req1", "req2"), "script2")
		))).thenAccept {
			assertFalse(it)
			evaluated1.tryComplete { true }
		}

		adminProxy.add(Command("idX", "nameX", "descriptionX", listOf(
			Command.Version("idX", listOf("req1", "req2"), "script1"),
			Command.Version("idXX", listOf("req1", "req2"), "script2")
		))).thenAccept {
			assert(it)
			evaluated2.tryComplete { true }
			expectedCommandsN++
			expectedVariantsN += 2
		}

		evaluated1.maxTimeToComplete(3000)
		evaluated2.maxTimeToComplete(3000)

		assert(evaluated1.result)
		assert(evaluated2.result)
	}

	@Test fun newVersionAddition() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		val result1 = ResultLock(true)
		val result2 = ResultLock(false)

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.add("Command1", Command.Version("vid0.0.0.1", listOf("req1", "req2"), "script2"))
			.thenAccept { result1.tryComplete { it } }

		adminProxy.add("Command1", Command.Version("vid0.0.0.2", listOf("req1", "req2"), "script2")).thenAccept {
			result2.tryComplete { it }
			expectedVariantsN++
		}

		result1.maxTimeToComplete(3000)
		result2.maxTimeToComplete(3000)

		assertFalse(result1.result)
		assert(result2.result)
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}