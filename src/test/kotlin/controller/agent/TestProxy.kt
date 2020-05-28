package controller.agent

import com.google.common.io.Resources
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import controller.agent.client.ClientAgent
import controller.agent.client.ClientProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import util.AgentTestUtil
import util.ResultLock
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test proxy-provided utilities
 *
 * @author Paolo Baldini
 */
class TestProxy {
	companion object {
		private val pathToMas = "kotlin/controller/agent/".replace("/", File.separator)
		private val masPath: String = Resources.getResource(pathToMas + "TestProxy.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()
	}

	@Test fun correctProxyType() {
		val proxyA1: AgentProxy = AdminProxy()
		val proxyA2: AgentProxy = AdminProxy()
		val proxyC1: AgentProxy = ClientProxy()
		val proxyC2: AgentProxy = ClientProxy()

		AgentUtils.startAgent(AdminAgent::class.java, proxyA1)
		AgentUtils.startAgent(ClientAgent::class.java, proxyA2, true, "client", "client@mail")
		AgentUtils.startAgent(AdminAgent::class.java, proxyC1)
		AgentUtils.startAgent(ClientAgent::class.java, proxyC2, true, "client", "client@mail")

		Thread.sleep(50)

		assert(proxyA1.isAvailable())
		assertFalse(proxyA2.isAvailable())
		assertFalse(proxyC1.isAvailable())
		assert(proxyC2.isAvailable())
	}

	@Test fun subscribeItemCommand() {
		val adminC = ResultLock(false)
		val adminI = ResultLock(false)
		val clientI = ResultLock(false)

		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)
		adminProxy.subscribeCommands(inlineOnNextObserver { adminC.tryComplete { true } })
		adminProxy.subscribeItems(inlineOnNextObserver { adminI.tryComplete { true } })

		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy, true, "client", "client@mail")
		clientProxy.subscribeItems(inlineOnNextObserver { clientI.tryComplete { true } })

		adminC.maxTimeToComplete(2000)
		adminI.maxTimeToComplete(2000)
		clientI.maxTimeToComplete(2000)

		assert(adminC.result)
		assert(adminI.result)
		assert(clientI.result)
	}

	// TODO: test other Request
	@Test fun partialAccept() {
		val adminProxy = AdminProxy()
		val clientProxy = ClientProxy()

		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy, true, "client", "client@mail")

		val admin = AtomicInteger()
		val client = AtomicInteger()

		adminProxy.subscribeCommands(inlineOnNextObserver { admin.incrementAndGet() })
		clientProxy.subscribeItems(inlineOnNextObserver { client.incrementAndGet() })

		// wait a single update cycle (from agent impl)
		Thread.sleep(1000)

		assertEquals(1, admin.get())
		assertEquals(1, client.get())

		adminProxy.stop()
		clientProxy.stop()

		assertEquals(1, admin.get())
		assertEquals(1, client.get())
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}