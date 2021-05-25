package framework

import jade.core.Agent
import jade.core.ProfileImpl
import jade.core.Runtime
import kotlin.random.Random

/**
 * Utilities for the start of a jade agent from it's main class
 *
 * @author Paolo Baldini
 */
object JADEAgents {

    fun <T: Agent>proxy(name: String, cls: Class<T>) = JADEProxy<T>().apply {
        Runtime.instance()
            .createAgentContainer(ProfileImpl())
            .createNewAgent(name + Random.nextDouble(), cls.canonicalName, arrayOf(this))
            .run { start() }
    }
}
