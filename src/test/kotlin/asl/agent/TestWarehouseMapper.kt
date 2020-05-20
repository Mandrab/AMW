package asl.agent

import asl.agent.fake.FakeOrderManagerAgent
import asl.agent.fake.FakeOrderManagerProxy
import com.google.common.io.Resources
import common.type.Item
import controller.agent.AgentUtils
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import controller.agent.client.ClientAgent
import controller.agent.client.ClientProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import util.ResultLock

class TestWarehouseMapper {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestWarehouseMapper.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()

		private var expected = mutableListOf(
			Item("\"Item 1\"", 0, listOf(
				Triple(5, 3, 5),
				Triple(5, 2, 8),
				Triple(6, 3, 7)
			)),
			Item("\"Item 2\"", 1, listOf(Triple(2, 4, 1))),
			Item("\"Item 3\"", 0, listOf(Triple(2, 5, 1))),
			Item("\"Item 4\"", 0, listOf(Triple(2, 5, 3)))
		)
	}

	@Test fun infoWarehouse() {
		// Start agent
		val clientProxy = ClientProxy()
		AgentUtils.startAgent(ClientAgent::class.java, clientProxy)

		val result = ResultLock<Collection<Item>>(emptyList())

		clientProxy.subscribeItems(inlineOnNextObserver { result.tryComplete { it } })

		result.maxTimeToComplete(3500)

		assert(result.result.isNotEmpty()) { "The warehouse must not be empty at the start of test.\n" +
				absTest.disclaimer }
		assertEquals("Expected items and result items should have the same size. Check changes of initial " +
				"belief. Obtained items from the warehouse:\n${result.result}\nExpected items:\n$expected",
			expected.size, result.result.size)
		assert(result.result.containsAll(expected)) {"Result items should be equals to expected ones. Check changes " +
				"of initial belief."}
	}

	@Test fun retrieveItems() {
		var agent: FakeOrderManagerAgent? = null
		val orderManagerProxy = FakeOrderManagerProxy { agent = it as FakeOrderManagerAgent }
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, orderManagerProxy)

		val firstResult = ResultLock(true)
		val secondResult = ResultLock(false)

		while (!orderManagerProxy.isAvailable() || agent == null) Thread.sleep(50)

		agent!!.retrieveItems("\"Order ID 1\"", listOf(Pair("Item 1", 1000), Pair("Item 2", 1000)))
			.thenAccept { firstResult.tryComplete { it } }
		agent!!.retrieveItems("\"Order ID 1\"", listOf(Pair("Item 1", 10), Pair("Item 3", 1), Pair("Item 4", 2)))
			.thenAccept { result ->
				secondResult.tryComplete {
					if (result) {
						expected.replaceAll {
							when (it.itemId) {
								"\"Item 1\"" -> Item(it.itemId, it.reserved + 10, it.positions)
								"\"Item 3\"" -> Item(it.itemId, it.reserved + 1, it.positions)
								"\"Item 4\"" -> Item(it.itemId, it.reserved + 2, it.positions)
								else -> it
							}
						}
					}; result
				}
			}

		firstResult.maxTimeToComplete(3000)
		secondResult.maxTimeToComplete(3000)

		assertFalse("The first item is required in too many copy so I expect a failure response.", firstResult.result)
		assert(secondResult.result) {"I expect the second item to be reserved."}
	}

	@Test fun addItem() {
		val adminProxy = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy)

		val successfullyAdded1 = ResultLock(false)
		val successfullyAdded2 = ResultLock(false)

		while (!adminProxy.isAvailable()) Thread.sleep(50)

		adminProxy.add("Item 1", 5, 6, 500).thenAccept { result ->
			successfullyAdded1.tryComplete {
				if (result) {
					expected.replaceAll {
						when (it.itemId) {
							"\"Item 1\"" -> Item(it.itemId, it.reserved, it.positions.toMutableList()
								.apply { add(Triple(5, 6, 500)) })
							else -> it
						}
					}
				}; result
			}
		}
		adminProxy.add("Item 5", 6, 7, 6).thenAccept { result ->
			successfullyAdded2.tryComplete {
				if (result) expected.add(Item("\"Item 5\"", 0, listOf(Triple(6, 7, 6))))
				result
			}
		}

		successfullyAdded1.maxTimeToComplete(3000)
		successfullyAdded2.maxTimeToComplete(3000)

		assert(successfullyAdded1.result) { "The addition of a new item is expected to be confirmed.\n" +
				absTest.disclaimer }
		assert(successfullyAdded2.result) { "The addition of a new item is expected to be confirmed.\n" +
				absTest.disclaimer }
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}