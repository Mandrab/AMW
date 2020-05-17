package asl.agent

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

		var evaluated = false

		while (!adminProxy.isAvailable()) Thread.sleep(50);

		adminProxy.subscribeCommands(inlineOnNextObserver { c ->
			assertEquals(expectedCommandsN, c.groupBy { it.id }.count())
			assertEquals(expectedVariantsN, c.count())
			evaluated = true;
		})

		Thread.sleep(2500)

		assert(evaluated)
	}

	@Test fun newCommandAddition() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		var evaluated1 = false
		var evaluated2 = false

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.add(Command("Command1", "nameX", "descriptionX", listOf(
			Command.Version("idX", listOf("req1", "req2"), "script1"),
			Command.Version("idXX", listOf("req1", "req2"), "script2")
		))).thenAccept {
			assertFalse(it)
			evaluated1 = true
		}

		adminProxy.add(Command("idX", "nameX", "descriptionX", listOf(
			Command.Version("idX", listOf("req1", "req2"), "script1"),
			Command.Version("idXX", listOf("req1", "req2"), "script2")
		))).thenAccept {
			assert(it)
			evaluated2 = true
			expectedCommandsN++
			expectedVariantsN += 2
		}

		Thread.sleep(3000)

		assert(evaluated1)
		assert(evaluated2)
	}

	@Test fun newVersionAddition() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		var evaluated1 = false
		var evaluated2 = false

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.add("Command1", Command.Version("vid0.0.0.1", listOf("req1", "req2"), "script2")).thenAccept {
			assertFalse(it)
			evaluated1 = true
		}

		adminProxy.add("Command1", Command.Version("vid0.0.0.2", listOf("req1", "req2"), "script2")).thenAccept {
			assert(it)
			evaluated2 = true
			expectedVariantsN++
		}

		Thread.sleep(3000)

		assert(evaluated1)
		assert(evaluated2)
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}