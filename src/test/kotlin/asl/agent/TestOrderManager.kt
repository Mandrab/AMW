package asl.agent

import com.google.common.io.Resources
import common.type.Order
import controller.agent.AgentUtils
import controller.agent.client.ClientAgent
import controller.agent.client.ClientProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import util.ResultLock

class TestOrderManager {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestOrderManager.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()
	}

	@Test fun placeOrderAndInfo() {
		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy, true, "client", "client@mail")

		val result = ResultLock(false)

		while (!clientProxy.isAvailable()) Thread.sleep(50)

		clientProxy.subscribeOrder(inlineOnNextObserver { if (it.status == Order.Status.COMPLETED) result.tryComplete { true } })
		clientProxy.placeOrder("client", "client@mail", "address", Pair("Item1", 5), Pair("Item2", 1), Pair("Item3", 1))

		result.maxTimeToComplete(15000)

		assert(result.result)
	}

	@Test fun noInfo() {
		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy, true, "ClIeNt", "ClIeNt@MaIl")

		val result = ResultLock(emptyList<Pair<String,Int>>())

		while (!clientProxy.isAvailable()) Thread.sleep(50)

		var i = 2
		clientProxy.subscribeOrder(inlineOnNextObserver { if (i-- == 0) result.tryComplete { it.items } })
		result.maxTimeToComplete(3000)

		assert(result.result.isEmpty())
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}