package framework

import framework.Framework.Utility.filterLogs
import framework.JADEAgents.proxy
import jade.core.Agent
import org.junit.Assert
import java.io.FilterOutputStream
import java.io.PrintStream
import kotlin.random.Random.Default.nextDouble

object Framework {
    private val caughtLogs = mutableListOf<String>()
    private val expectedLogs = mutableListOf<String>()

    val agents = HashMap<String, Agent>()
    var recordLogs: Boolean = false

    fun test(filterLogs: Boolean = false, action: Framework.() -> Unit) = apply { if (filterLogs) filterLogs() }
        .apply(action).run {
            agents.values.onEach(Agent::doDelete)
            agents.clear()

            caughtLogs.removeAll { ! expectedLogs.contains(it) }
            Assert.assertArrayEquals("$caughtLogs\n$expectedLogs", caughtLogs.toTypedArray(), expectedLogs.toTypedArray())

            caughtLogs.clear()
            expectedLogs.clear()
        }

    operator fun String.unaryMinus() = expectedLogs.add(this)

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

    object Utility {
        val agent get() = agents["agent"]?.let { it as JADEAgent } ?: agent("agent", JADEAgent::class.java)

        fun filterLogs() {
            val filterStream = object: FilterOutputStream(System.err) {
                override fun write(b: ByteArray, off: Int, len: Int) {
                    b.decodeToString().split("\n")
                        .filter {
                            it.contains("INFO: [") || it.contains("SEVERE: [")
                        }.map {
                            when {
                                it.startsWith("INFO: ") -> it.removePrefix("INFO: ")
                                else -> it
                            }
                        }.forEach {
                            if (recordLogs) caughtLogs.add(it)
                            super.write((it + "\n").toByteArray(), 0, it.length + 1)
                        }
                }
            }
            System.setErr(PrintStream(filterStream, true))
        }
    }
}
