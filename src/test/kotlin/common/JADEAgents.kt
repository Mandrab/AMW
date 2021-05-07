package common

import jade.core.Agent
import jade.core.ProfileImpl
import jade.core.Runtime
import java.util.concurrent.Semaphore
import kotlin.random.Random

object JADEAgents {

    fun <T: Agent>proxy(name: String, cls: Class<T>) = JADEProxy<T>().apply {
        Runtime.instance()
            .createAgentContainer(ProfileImpl())
            .createNewAgent(name + Random.nextDouble(), cls.canonicalName, arrayOf(this))
            .run { start() }
    }
}
