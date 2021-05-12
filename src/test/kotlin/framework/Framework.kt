package framework

import common.ontology.Services.ServiceType.*
import common.ontology.Services.ServiceSupplier.*
import framework.JADEAgents.proxy
import jade.core.Agent
import jade.lang.acl.ACLMessage
import org.junit.Assert
import kotlin.random.Random.Default.nextDouble

object Framework {
    const val waitingTime = 500L
    const val retryTime = 2000L

    private val agents = HashMap<String, Agent>()

    fun test(action: Framework.() -> Unit) = run(action).apply { agents.values.onEach(Agent::doDelete); agents.clear() }

    fun agent() = agent(nextDouble().toString(), JADEAgent::class.java)

    fun <T: Agent>agent(cls: Class<T>) = agent(nextDouble().toString(), cls)

    fun <T: Agent>agent(name: String, cls: Class<T>): T = when(cls.name) {
        ASLAgent::class.java.name ->
            @Suppress("UNCHECKED_CAST")
            ASLAgents.start(name) as T
        else -> proxy(name, cls).getAgent()
    }.apply { agents[name] = this }

    fun oneshotAgent(action: JADEAgent.() -> Unit) = oneshotAgent(JADEAgent::class.java, action)

    fun <T: Agent>oneshotAgent(cls: Class<T>, action: T.() -> Unit) = oneshotAgent(nextDouble().toString(), cls, action)

    fun <T: Agent>oneshotAgent(name: String, cls: Class<T>, action: T.() -> Unit) = when(cls.name) {
        ASLAgent::class.java.name ->
            @Suppress("UNCHECKED_CAST")
            ASLAgents.start(name) as T
        else -> proxy(name, cls).getAgent()
    }.apply(action).doDelete()

    fun assert(message: ACLMessage, performative: Int, content: Any) {
        Assert.assertNotNull(message)
        Assert.assertEquals(performative, message.performative)
        if (content is String) Assert.assertEquals(content, message.content)
        else Assert.assertEquals(content.toString().trim(), message.content)
    }

    object Utility {
        val agent get() = agents["agent"]?.let { it as JADEAgent } ?: agent("agent", JADEAgent::class.java)
        val mid = Messaging.MID()
    }

    object JADE {
        val orderManager get() = getOrBuild("jade_order_manager") {
                register(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id, INFO_ORDERS.id)
            }
        val warehouseMapper get() = getOrBuild("jade_warehouse_mapper") {
                register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id, STORE_ITEM.id, INFO_WAREHOUSE.id)
            }
        val robotPicker get() = getOrBuild("jade_robot_picker") {
                register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
            }
        val collectionPointManager get() = getOrBuild("jade_collection_point_manager") {
                register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
            }
        val commandManager get() = getOrBuild("jade_command_manager") {
            register(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id)
        }

        private fun getOrBuild(name: String, init: JADEAgent.() -> JADEAgent): JADEAgent =
            agents[name]?.let { it as JADEAgent } ?: agent(name, JADEAgent::class.java).init()
    }

    object ASL {
        val orderManager get() = getOrBuild("order_manager")
        val warehouseMapper get() = getOrBuild("warehouse_mapper")
        val robotPicker get() = getOrBuild("robot_picker")
        val collectionPointManager get() = getOrBuild("collection_point_manager")
        val commandManager get() = getOrBuild("command_manager")

        private fun getOrBuild(name: String) = agents[name]?.let { it as ASLAgent } ?: agent(name, ASLAgent::class.java)
    }
}
