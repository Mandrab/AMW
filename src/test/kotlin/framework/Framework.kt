package framework

import framework.Framework.Utility.filterLogs
import framework.JADEAgents.proxy
import jade.core.Agent
import org.junit.Assert
import java.io.FilterOutputStream
import java.io.PrintStream
import java.lang.Exception
import kotlin.random.Random.Default.nextDouble

object Framework {
    private val caughtLogs = mutableListOf<String>()
    private val expectedLogs = mutableListOf<String>()

    val agents = HashMap<String, Agent>()
    var recordLogs: Boolean = false

    fun test(filterLogs: Boolean = false, action: Framework.() -> Unit) = apply {
        if (filterLogs) filterLogs()
        try {
            action()
        } catch (e: Exception) {
            closeTest()
            throw e
        }
    }.run { closeTest() }

    private fun closeTest() {
        agents.values.onEach(Agent::doDelete)
        agents.clear()

        val tmp1 = caughtLogs.apply { clear() }
        val tmp2 = expectedLogs.apply { clear() }

        tmp1.removeAll { ! tmp2.contains(it) }
        Assert.assertArrayEquals("$tmp1\n$tmp2", tmp1.toTypedArray(), tmp2.toTypedArray())
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
