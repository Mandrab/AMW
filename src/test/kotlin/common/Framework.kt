package common

import common.JADEAgents.proxy
import jade.core.Agent
import org.junit.AfterClass
import kotlin.random.Random

abstract class Framework {
    companion object {
        private val agents = mutableListOf<Agent>()

        @AfterClass fun terminate() {
            ASLAgents.killAll()
            agents.forEach(Agent::doDelete)
        }
    }

    fun agent() = agent(Random.nextDouble().toString(), JADEAgent::class.java)

    fun <T: Agent>agent(cls: Class<T>) = agent(Random.nextDouble().toString(), cls)

    fun <T: Agent>agent(name: String, cls: Class<T>): T = when(cls.name) {
        ASLAgent::class.java.name ->
            @Suppress("UNCHECKED_CAST")
            ASLAgents.start(name) as T
        else -> proxy(name, cls).getAgent()
    }.apply(agents::add)

    fun oneshotAgent(action: JADEAgent.() -> Unit) = oneshotAgent(JADEAgent::class.java, action)

    fun <T: Agent>oneshotAgent(cls: Class<T>, action: T.() -> Unit) =
        oneshotAgent(Random.nextDouble().toString(), cls, action)

    fun <T: Agent>oneshotAgent(name: String, cls: Class<T>, action: T.() -> Unit) = when(cls.name) {
        ASLAgent::class.java.name ->
            @Suppress("UNCHECKED_CAST")
            ASLAgents.start(name) as T
        else -> proxy(name, cls).getAgent()
    }.apply(action).doDelete()

    fun test(action: Framework.() -> Unit) = object: Framework() { }.run(action)
}
