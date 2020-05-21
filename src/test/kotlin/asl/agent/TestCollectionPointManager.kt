package asl.agent

import asl.agent.fake.FakeOrderManagerAgent
import asl.agent.fake.FakeOrderManagerProxy
import com.google.common.io.Resources
import controller.agent.AgentUtils
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import util.ResultLock

class TestCollectionPointManager {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestCollectionPointManager.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()
	}

	@Test fun checkProposeTimeout() {
		var agent: FakeOrderManagerAgent? = null
		val proxy = FakeOrderManagerProxy { agent = it as FakeOrderManagerAgent }
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, proxy)

		val confirm = ResultLock(false)

		while (agent == null) Thread.sleep(50)

		agent!!.getCollectionPoint(false).thenAccept {
			confirm.tryComplete { agent!!.waitMsg() }
		}

		confirm.maxTimeToComplete(2500)

		assert(confirm.result)
	}

	@Test fun checkRequestAndFree() {
		var agent: FakeOrderManagerAgent? = null
		val proxy = FakeOrderManagerProxy { agent = it as FakeOrderManagerAgent }
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, proxy)

		while (agent == null) Thread.sleep(50)

		for (cycle in 0..3) {
			var confirm = Array<Boolean?>(9) { null }

			confirm.indices.forEach { i -> agent!!.getCollectionPoint().thenAccept { confirm[i] = it } }

			while (confirm.any { it == null }) Thread.sleep(100)

			assertEquals(cycle.toString(), 3, confirm.count { it != null && it })

			val confirmFree = Array<Boolean?>(3) { null }
			confirmFree.indices.onEach { i -> agent!!.freeCollectionPoint().thenAccept { confirmFree[i] = it } }

			while (confirmFree.any { it == null }) Thread.sleep(100)

			assertEquals(3, confirmFree.count { it != null && it })

			confirm = Array(9) { null }
		}
	}
}