package controller.agent

import com.google.common.io.Resources
import common.Request
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
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test proxy-provided utilities
 *
 * @author Paolo Baldini
 */
class TestProxy {
	companion object {
		private val masPath: String = Resources.getResource("TestProxy.mas2j").path
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
		AgentUtils.startAgent(ClientAgent::class.java, proxyA2)
		AgentUtils.startAgent(AdminAgent::class.java, proxyC1)
		AgentUtils.startAgent(ClientAgent::class.java, proxyC2)

		Thread.sleep(50)

		assert(proxyA1.isAvailable())
		assertFalse(proxyA2.isAvailable())
		assertFalse(proxyC1.isAvailable())
		assert(proxyC2.isAvailable())
	}

	@Test fun subscribeItemCommand() {
		val adminC = AtomicInteger()
		val adminI = AtomicInteger()
		val clientI = AtomicInteger()

		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)
		adminProxy.subscribeCommands(inlineOnNextObserver { adminC.incrementAndGet() })
		adminProxy.subscribeItems(inlineOnNextObserver { adminI.incrementAndGet() })

		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy)
		clientProxy.subscribeItems(inlineOnNextObserver { clientI.incrementAndGet() })

		Thread.sleep(2000)

		assert(adminC.get() > 0)
		assert(adminI.get() > 0)
		assert(clientI.get() > 0)
	}

	// TODO: test other Request
	@Test fun partialAccept() {
		val adminProxy = AdminProxy()
		val clientProxy = ClientProxy()

		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy)

		val admin = AtomicInteger()
		val client = AtomicInteger()

		adminProxy.subscribeCommands(inlineOnNextObserver { admin.incrementAndGet() })
		clientProxy.subscribeItems(inlineOnNextObserver { client.incrementAndGet() })

		Thread.sleep(1000)

		assertEquals(1, admin.get())
		assertEquals(1, client.get())

		adminProxy.accept(Pair(Request.END, emptyArray()))
		clientProxy.accept(Pair(Request.END, emptyArray()))

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