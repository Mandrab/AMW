package asl.agent.fake

import controller.agent.AgentProxy
import jade.core.Agent

class FakeOrderManagerProxy(val action: (Agent) -> Unit) : AgentProxy {

	override fun setAgent(agent: Agent) = action(agent)

	override fun isAvailable(): Boolean = true
}