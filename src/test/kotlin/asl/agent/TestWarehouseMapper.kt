package asl.agent

import asl.agent.fake.FakeOrderManagerAgent
import asl.agent.fake.FakeOrderManagerProxy
import com.google.common.io.Resources
import common.type.Item
import controller.agent.AgentUtils
import controller.agent.client.ClientAgent
import controller.agent.client.ClientProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test

class TestWarehouseMapper {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestWarehouseMapper.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()

		var expectedCommandsN = 2
		var expectedVariantsN = 3
	}

	private val expected = mutableListOf(
		Item("\"Item 1\"", 0, listOf(
			Triple(5, 3, 5),
			Triple(5, 2, 8),
			Triple(6, 3, 7)
		)),
		Item("\"Item 2\"", 1, listOf(Triple(2, 4, 1))),
		Item("\"Item 3\"", 0, listOf(Triple(2, 5, 1))),
		Item("\"Item 4\"", 0, listOf(Triple(2, 5, 3)))
	)

	@Test fun infoWarehouse() {
		// Start agent
		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy)

		var result: Collection<Item> = emptyList()

		clientProxy.subscribeItems(inlineOnNextObserver { result = it })

		Thread.sleep(2000)

		assert(result.isNotEmpty())
		assertEquals(expected.size, result.size)
		assert(result.containsAll(expected))
	}

	@Test fun retrieveItems() {
		var agent: FakeOrderManagerAgent? = null
		val orderManagerProxy = FakeOrderManagerProxy { agent = it as FakeOrderManagerAgent }
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, orderManagerProxy)

		var firstResult = true
		var secondResult = false

		while (!orderManagerProxy.isAvailable() || agent == null) Thread.sleep(50)

		agent!!.retrieveItems("\"Order ID 1\"", listOf(Pair("Item 1", 1000), Pair("Item 2", 1000)))
			.thenAccept { firstResult = it }
		agent!!.retrieveItems("\"Order ID 1\"", listOf(Pair("Item 1", 10), Pair("Item 3", 1), Pair("Item 4", 2)))
			.thenAccept { it ->
				secondResult = it
				expected.replaceAll { 
					when (it.itemId) {
						"\"Item 1\"" -> Item(it.itemId, it.reserved + 10, it.positions)
						"\"Item 3\"" -> Item(it.itemId, it.reserved + 1, it.positions)
						"\"Item 4\"" -> Item(it.itemId, it.reserved + 2, it.positions)
						else -> it
					}
				}
			}

		Thread.sleep(2500)

		assertFalse(firstResult)
		assert(secondResult)
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}