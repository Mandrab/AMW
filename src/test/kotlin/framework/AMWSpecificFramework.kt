package framework

import common.ontology.Services.ServiceType.*
import common.ontology.Services.ServiceSupplier.*
import jade.core.Agent

/**
 * Collection of testing utilities strictly related to AMW system
 *
 * @author Paolo Baldini
 */
object AMWSpecificFramework {
    const val waitingTime = 500L                                            // reasonable time for message delivery
    const val retryTime = 2000L                                             // time between message resend

    fun oid(i: Int) = """\(\[a-z\|0-9\|\.\|_\]\*odr$i\)"""
    fun mid(i: Int) = "mid(mid$i)"

    object Test {
        val admin get() = getOrBuild("admin_agent", controller.admin.agent.Agent::class.java)
        val user get() = getOrBuild("user_agent", controller.user.agent.Agent::class.java)
        val communicator get() = getOrBuild("communicator_agent",
            controller.agent.communication.Communicator::class.java)

        @Suppress("UNCHECKED_CAST")
        private fun <T: Agent> getOrBuild(name: String, cls: Class<T>): T = Framework.agents[name]
            ?.let { it as T }
            ?: Framework.agent(name, cls)
    }

    object JADE {
        val orderManager get() = getOrBuild("jade_order_manager") {
            register(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id, INFO_ORDERS.id)
        }
        val warehouseMapper get() = getOrBuild("jade_warehouse_mapper") {
            register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id, STORE_ITEM.id, INFO_WAREHOUSE.id)
        }
        val robotPicker get() = getOrBuild("jade_robot_picker") {
            register(PICKER_ITEMS.id to RETRIEVE_ITEM.id, EXECUTOR_COMMAND.id to EXEC_COMMAND.id)
        }
        val collectionPointManager get() = getOrBuild("jade_collection_point_manager") {
            register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
        }
        val commandManager get() = getOrBuild("jade_command_manager") {
            register(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id, INFO_COMMANDS.id)
        }

        private fun getOrBuild(name: String, init: JADEAgent.() -> JADEAgent): JADEAgent = Framework.agents[name]
                ?.let { it as JADEAgent }
                ?: Framework.agent(name, JADEAgent::class.java).init()
    }

    object ASL {
        val orderManager get() = getOrBuild("order_manager")
        val warehouseMapper get() = getOrBuild("warehouse_mapper")
        val robotPicker get() = getOrBuild("robot_picker")
        val collectionPointManager get() = getOrBuild("collection_point_manager")
        val commandManager get() = getOrBuild("command_manager")

        private fun getOrBuild(name: String): ASLAgent = Framework.agents[name]
            ?.let { it as ASLAgent }
            ?: Framework.agent(name, ASLAgent::class.java)
    }
}
