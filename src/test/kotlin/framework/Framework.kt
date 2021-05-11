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

    fun test(action: Framework.() -> Unit) = run(action).apply { agents.values.onEach(Agent::doDelete).clear() }

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

    val orderManager: JADEAgent get() = getOrBuild("order_manager") {
        register(MANAGEMENT_ORDERS.id, ACCEPT_ORDER.id, INFO_ORDERS.id)
    }
    val warehouseMapper: JADEAgent get() = getOrBuild("warehouse_mapper") {
        register(MANAGEMENT_ITEMS.id, REMOVE_ITEM.id, STORE_ITEM.id, INFO_WAREHOUSE.id)
    }
    val robotPicker: JADEAgent get() = getOrBuild("robot_picker") {
        register(PICKER_ITEMS.id, RETRIEVE_ITEM.id)
    }
    val collectionPointManager: JADEAgent get() = getOrBuild("collection_point_manager") {
        register(MANAGEMENT_ITEMS.id, INFO_COLLECTION_POINTS.id)
    }
    val commandManager = getOrBuild("command_manager") {
        register(MANAGEMENT_COMMANDS.id, ADD_COMMAND.id)
    }

    private fun getOrBuild(name: String, init: JADEAgent.() -> JADEAgent): JADEAgent =
        agents[name] ?.let { it as JADEAgent } ?: agent(name, JADEAgent::class.java).init()
}
