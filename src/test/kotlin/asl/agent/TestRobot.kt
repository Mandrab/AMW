package asl.agent

import asl.agent.fake.FakeAdminAgent
import asl.agent.fake.FakeAminProxy
import asl.agent.fake.FakeOrderManagerAgent
import asl.agent.fake.FakeOrderManagerProxy
import com.google.common.io.Resources
import controller.agent.AgentUtils
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import jade.lang.acl.ACLMessage
import util.AgentTestUtil
import org.junit.AfterClass
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import util.ResultLock
import java.io.File
import java.util.function.Function
import kotlin.concurrent.withLock

class TestRobot {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestRobot.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()

		private var executedTests = 5
	}

	@Test fun scriptExecution() {
		--executedTests

		// Start agents
		val adminProxy1 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy1)
		val adminProxy2 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy2)
		val adminProxy3 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy3)

		// create test variables
		val firstScriptIsValid = ResultLock(true)
		val evaluatedWithSuccess1 = ResultLock(Pair(first = false, second = false))
		val evaluatedWithSuccess2 = ResultLock(Pair(first = false, second = false))

		// load script
		val script = Resources.getResource("asl${File.separator}script${File.separator}test_robot_script.asl").readText()

		// wait till agents are started
		while (!adminProxy1.isAvailable() || !adminProxy2.isAvailable() || !adminProxy3.isAvailable()) Thread.sleep(50)

		// should respond refusing
		adminProxy1.execute(script, setOf("?!£$%&/()=")).thenAccept { firstScriptIsValid.tryComplete { it } }
		// with two correct request, one should succeed and one should fail
		adminProxy2.execute(script, emptySet()).thenAccept {
			evaluatedWithSuccess1.withLock { evaluatedWithSuccess1.tryComplete { Pair(true, it) } }
		}
		adminProxy3.execute(script, emptySet()).thenAccept {
			evaluatedWithSuccess1.withLock { evaluatedWithSuccess2.tryComplete { Pair(true, it) } }
		}

		firstScriptIsValid.maxTimeToComplete(4000)
		evaluatedWithSuccess1.maxTimeToComplete(4000)
		evaluatedWithSuccess2.maxTimeToComplete(4000)

		assertFalse(firstScriptIsValid.result)
		assert(evaluatedWithSuccess1.result.first)
		assert(evaluatedWithSuccess2.result.first)
		assert(evaluatedWithSuccess1.result.second != evaluatedWithSuccess2.result.second)

		// wait some time to let the script end it's execution
		if (executedTests > 0) Thread.sleep(2000)
	}

	@Test fun testScriptResponseTimeout() {
		--executedTests

		// Start agent
		var fakeAdminAgent: FakeAdminAgent? = null
		AgentUtils.startAgent(FakeAdminAgent::class.java, FakeAminProxy { fakeAdminAgent = it as FakeAdminAgent })

		// create test variables
		val requestAccepted = ResultLock(false)
		val acceptanceExpired = ResultLock(false)

		// load script
		val script = Resources.getResource("asl${File.separator}script${File.separator}test_robot_script.asl").readText()

		// wait till agent is started
		while (fakeAdminAgent == null) Thread.sleep(50)

		// i expect an acceptance for script execution but a fail after not receive confirm
		fakeAdminAgent!!.executeButNotRespond(script, Function {
			return@Function it != null && it.performative == ACLMessage.PROPOSE
		}).thenAccept {
			requestAccepted.tryComplete { it }
			acceptanceExpired.tryComplete { fakeAdminAgent!!.waitMsg() }
		}

		requestAccepted.maxTimeToComplete(4000)
		acceptanceExpired.maxTimeToComplete(4000)

		assert(requestAccepted.result)
		assert(acceptanceExpired.result)
	}

	@Test fun itemPicking() {
		--executedTests

		// Start agents
		var fakeAgent1: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
				fakeAgent1 = it as FakeOrderManagerAgent })
		var fakeAgent2: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
			fakeAgent2 = it as FakeOrderManagerAgent })

		// create test variables
		val evaluatedWithSuccess1 = ResultLock(Pair(first = false, second = false))
		val evaluatedWithSuccess2 = ResultLock(Pair(first = false, second = false))

		// wait till agents are started
		while (fakeAgent1 == null || fakeAgent2 == null) Thread.sleep(50)

		val message = "retrieve(id(Id), item(item(Item)[todo]))"

		// with two correct request, one should succeed and one should fail
		fakeAgent1!!.retrieveItem(message).thenAccept {
			evaluatedWithSuccess1.withLock { evaluatedWithSuccess1.tryComplete { Pair(true, it) } }
		}
		fakeAgent1!!.retrieveItem(message).thenAccept {
			evaluatedWithSuccess1.withLock { evaluatedWithSuccess2.tryComplete { Pair(true, it) } }
		}
		evaluatedWithSuccess1.maxTimeToComplete(4000)
		evaluatedWithSuccess2.maxTimeToComplete(4000)

		assert(evaluatedWithSuccess1.result.first)
		assert(evaluatedWithSuccess2.result.first)
		assert(evaluatedWithSuccess1.result.second != evaluatedWithSuccess2.result.second)

		// wait some time to let the picking end it's execution
		if (executedTests > 0) Thread.sleep(2000)
	}

	@Test fun testItemPickingResponseTimeout() {
		--executedTests

		// Start agents
		var fakeAgent: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
			fakeAgent = it as FakeOrderManagerAgent })

		// create test variables
		val requestAccepted = ResultLock(false)
		val acceptanceExpired = ResultLock(false)

		val message = "retrieve(id(Id), item(item(Item)[todo]))"

		// wait till agent is started
		while (fakeAgent == null) Thread.sleep(50)

		// i expect an acceptance for script execution but a fail after not receive confirm
		fakeAgent!!.retrieveButNotRespond(message, Function {
			return@Function it != null &&  it.performative == ACLMessage.PROPOSE
		}).thenAccept {
			requestAccepted.tryComplete { it }
			acceptanceExpired.tryComplete { fakeAgent!!.waitMsg() }
		}

		requestAccepted.maxTimeToComplete(4000)
		acceptanceExpired.maxTimeToComplete(4000)

		assert(requestAccepted.result)
		assert(acceptanceExpired.result)
	}

	@Test fun commandExecution() {
		--executedTests

		// Start agents
		val adminProxy1 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy1)
		val adminProxy2 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy2)
		val adminProxy3 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy3)

		// create test variables
		val firstCommandIsValid = ResultLock(true)
		val evaluatedWithSuccess1 = ResultLock(Pair(first = false, second = false))
		val evaluatedWithSuccess2 = ResultLock(Pair(first = false, second = false))

		// wait till agents are started
		while (!adminProxy1.isAvailable() || !adminProxy2.isAvailable() || !adminProxy3.isAvailable()) Thread.sleep(50)

		// should respond refusing
		adminProxy1.execute("?!£$%&").thenAccept {
			firstCommandIsValid.tryComplete { it }
			// with two correct request, one should succeed and one should fail
			adminProxy2.execute("Command1").thenAccept {
				evaluatedWithSuccess1.withLock { evaluatedWithSuccess1.tryComplete { Pair(true, it) } }
			}
			adminProxy3.execute("Command1").thenAccept {
				evaluatedWithSuccess1.withLock { evaluatedWithSuccess2.tryComplete { Pair(true, it) } }
			}
		}

		firstCommandIsValid.maxTimeToComplete(4000)
		evaluatedWithSuccess1.maxTimeToComplete(4000)
		evaluatedWithSuccess2.maxTimeToComplete(4000)

		assertFalse(firstCommandIsValid.result)
		assert(evaluatedWithSuccess1.result.first)
		assert(evaluatedWithSuccess2.result.first)
		assert(evaluatedWithSuccess1.result.second != evaluatedWithSuccess2.result.second)

		// wait some time to let the command end it's execution
		if (executedTests > 0) Thread.sleep(2000)
	}
}