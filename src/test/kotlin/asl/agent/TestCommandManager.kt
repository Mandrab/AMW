package asl.agent

import com.google.common.io.Resources
import controller.agent.AgentUtils
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class TestCommandManager {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestCommandManager.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()
	}

	@Test fun commandsLoading() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		adminProxy.subscribeCommands(inlineOnNextObserver { c ->
			assertEquals(2, c.count())
			assertEquals(3, c.flatMap { it.versions }.count())
		})

		Thread.sleep(3000)
	}

	@Test fun commandsAddition() {
		//TODO()
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}