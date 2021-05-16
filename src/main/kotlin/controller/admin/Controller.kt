package controller.admin

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
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

    override fun addCommand(command: Command) = proxy.addCommand(command)

    override fun addItem(item: WarehouseItem) = proxy.addItem(item)

    override fun removeItem(item: QuantityItem) = proxy.removeItem(item)

    override fun executeCommand(id: ID) = proxy.executeCommand(id)

    override fun warehouseState() = proxy.warehouseState()

    override fun stopSystem() = proxy.shutdown()
}
