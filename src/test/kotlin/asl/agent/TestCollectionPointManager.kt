package asl.agent

import asl.agent.fake.FakeOrderManagerAgent
import asl.agent.fake.FakeOrderManagerProxy
import com.google.common.io.Resources
import controller.agent.AgentUtils
import util.AgentTestUtil
import org.junit.AfterClass
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

	@Test fun checkPropose() {
		var agent: FakeOrderManagerAgent? = null
		val proxy = FakeOrderManagerProxy { agent = it as FakeOrderManagerAgent }
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, proxy)

		val confirm = ResultLock(false)

		while (agent == null) Thread.sleep(50)

		agent!!.getCollectionPoint().thenAccept { confirm.tryComplete { it } }

		confirm.maxTimeToComplete(2500)

		assert(confirm.result)
	}
}