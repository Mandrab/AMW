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
import java.io.File
import java.util.function.Function

class TestRobot {
	companion object {
		private val masPath: String = Resources.getResource("asl/TestRobot.mas2j").path
		private val absTest = AgentTestUtil()
		@BeforeClass @JvmStatic fun init() = absTest.startMAS(masPath)
		@AfterClass @JvmStatic fun end() = absTest.endContainer()
	}

	@Test fun scriptExecution() {
		// Start agents
		val adminProxy1 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy1)
		val adminProxy2 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy2)
		val adminProxy3 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy3)

		// create test variables
		val synch = Any()
		var evaluated1 = false
		var evaluated2 = false
		var evaluated3 = false

		// load script
		val script = Resources.getResource("asl${File.separator}script${File.separator}test_robot_script.asl").readText()

		// wait till agents are started
		while (!adminProxy1.isAvailable() && !adminProxy2.isAvailable() && !adminProxy3.isAvailable()) Thread.sleep(50)

		// should respond refusing
		adminProxy1.execute(script, setOf("?!£$%&/()=")).thenAccept { assertFalse(it).apply { evaluated1 = true } }
		// with two correct request, one should succeed and one should fail
		adminProxy2.execute(script, emptySet()).thenAccept { synchronized(synch) {
			assert((it && !evaluated3) || (evaluated3 && !it)).apply { evaluated2 = true } } }
		adminProxy3.execute(script, emptySet()).thenAccept { synchronized(synch) {
			assert((it && !evaluated2) || (evaluated2 && !it)).apply { evaluated3 = true } } }

		Thread.sleep(4000)

		assert(evaluated1)
		assert(evaluated2)
		assert(evaluated3)
	}

	@Test fun testScriptResponseTimeout() {
		// Start agent
		var fakeAdminAgent: FakeAdminAgent? = null
		AgentUtils.startAgent(FakeAdminAgent::class.java, FakeAminProxy { fakeAdminAgent = it as FakeAdminAgent })

		// create test variables
		var evaluated1 = false
		var evaluated2 = false

		// load script
		val script = Resources.getResource("asl${File.separator}script${File.separator}test_robot_script.asl").readText()

		// wait till agent is started
		while (fakeAdminAgent == null) Thread.sleep(50)

		// i expect an acceptance for script execution but a fail after not receive confirm
		fakeAdminAgent!!.executeButNotRespond(script, Function {
			return@Function it != null &&  it.performative == ACLMessage.PROPOSE
		}).thenAccept {
			assert(true).apply { evaluated1 = true }
			evaluated2 = fakeAdminAgent!!.waitMsg()
		}

		Thread.sleep(4000)

		assert(evaluated1)
		assert(evaluated2)
	}

	@Test fun itemPicking() {
		// Start agents
		var fakeAgent1: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
				fakeAgent1 = it as FakeOrderManagerAgent })
		var fakeAgent2: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
			fakeAgent2 = it as FakeOrderManagerAgent })

		// create test variables
		val synch = Any()
		var evaluated1 = false
		var evaluated2 = false

		// wait till agents are started
		while (fakeAgent1 == null || fakeAgent2 == null) Thread.sleep(50)

		val message = "retrieve(id(Id), item(item(Item)[todo]))"

		// with two correct request, one should succeed and one should fail
		fakeAgent1!!.retrieve(message).thenAccept { synchronized(synch) {
			assert((it && !evaluated2) || (evaluated2 && !it)).apply { evaluated1 = true } } }
		fakeAgent1!!.retrieve(message).thenAccept { synchronized(synch) {
			assert((it && !evaluated1) || (evaluated1 && !it)).apply { evaluated2 = true } } }

		Thread.sleep(4000)

		assert(evaluated1)
		assert(evaluated2)
	}

	@Test fun testItemPickingResponseTimeout() {
		/// Start agents
		var fakeAgent: FakeOrderManagerAgent? = null
		AgentUtils.startAgent(FakeOrderManagerAgent::class.java, FakeOrderManagerProxy {
			fakeAgent = it as FakeOrderManagerAgent })

		// create test variables
		var evaluated1 = false
		var evaluated2 = false

		val message = "retrieve(id(Id), item(item(Item)[todo]))"

		// wait till agent is started
		while (fakeAgent == null) Thread.sleep(50)

		// i expect an acceptance for script execution but a fail after not receive confirm
		fakeAgent!!.retrieveButNotRespond(message, Function {
			return@Function it != null &&  it.performative == ACLMessage.PROPOSE
		}).thenAccept {
			assert(true).apply { evaluated1 = true }
			evaluated2 = fakeAgent!!.waitMsg()
		}

		Thread.sleep(4000)

		assert(evaluated1)
		assert(evaluated2)
	}

	@Test fun commandExecution() {
		// Start agents
		val adminProxy1 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy1)
		val adminProxy2 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy2)
		val adminProxy3 = AdminProxy()
		AgentUtils.startAgent(AdminAgent::class.java, adminProxy3)

		// create test variables
		val synch = Any()
		var evaluated1 = false
		var evaluated2 = false
		var evaluated3 = false

		var res2 = false
		var res3 = false

		// wait till agents are started
		while (!adminProxy1.isAvailable() && !adminProxy2.isAvailable() && !adminProxy3.isAvailable()) Thread.sleep(50)

		// should respond refusing
		adminProxy1.execute("?!£$%&").thenAccept {
			assertFalse(it).apply { evaluated1 = true }
			// with two correct request, one should succeed and one should fail
			adminProxy2.execute("Command1").thenAccept {
				synchronized(synch) {
					res2 = it
					evaluated2 = true
				}
			}
			adminProxy3.execute("Command1").thenAccept {
				synchronized(synch) {
					res3 = it
					evaluated3 = true
				}
			}
		}

		Thread.sleep(4000)

		assert(evaluated1)
		assert(evaluated2 && evaluated3)
		assert((res2 && !res3) || (!res2 && res3))
	}
}