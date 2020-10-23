package controller.admin.agent

import controller.agent.AgentProxy

object Proxy {

    interface Proxy: AgentProxy<Agent> {

        fun addCommand()

        fun addVersion()

        fun executeCommand()

        fun executeScript()
    }

    operator fun invoke(): Proxy = AdminProxy()

    private class AdminProxy: Proxy {
        private lateinit var agent: Agent

        override fun setAgent(agent: Agent) { this.agent = agent }

        override fun isAvailable() = this::agent.isInitialized

        override fun shutdown() = agent.shutdown()

        override fun addCommand() = agent.addCommand()

        override fun addVersion() = agent.addVersion()

        override fun executeCommand() = agent.executeCommand()

        override fun executeScript() = agent.executeScript()
    }
}