package controller.admin

import controller.Controller.Admin
import controller.admin.agent.Agent
import controller.agent.Agents
import controller.admin.agent.Proxy
import view.View

/**
 * Main class of the application that creates agent and manage main data flow
 *
 * @param retryConnection run application keeping retry connection if it fails
 *
 * @author Paolo Baldini
 */
class Controller(retryConnection: Boolean = true): Admin {
    private val proxy = Proxy()

    init {
        Agents.start(retryConnection)(arrayOf(proxy, "user", "user@mail"))(Agent::class.java)
        View(this)
    }

    override fun addCommand() = proxy.addCommand()

    override fun addVersion() = proxy.addVersion()

    override fun executeCommand() = proxy.executeCommand()

    override fun executeScript() = proxy.executeScript()

    override fun warehouseState() = proxy.warehouseState()

    override fun stopSystem() = proxy.shutdown()
}