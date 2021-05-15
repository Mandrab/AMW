package framework

import jade.core.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.UnreadableException
import jade.tools.sniffer.Agent.i
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.Assert


/**
 * Define a DSL for communication and testing
 *  - '..' send the message
 *  - '<, >, =='
 *      - define an equality test between a reception and an expected value
 *      - send a message to an agent
 *  - '+' adds a content to a message
 *  - '-' adds a message id
 *
 * @author Paolo Baldini
 */
object Messaging {
    private const val waitingTime = 500L

    class Message(performative: Int): ACLMessage(performative) {
        lateinit var senderAgent: Agent
    }

    operator fun Agent.rangeTo(message: Message) = message.apply { senderAgent = this@rangeTo; sender = aid }

    operator fun Agent.compareTo(message: Message) = 0.apply {
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull("An expected message has not arrived", result)
        Assert.assertEquals("Performative differs from expectations", message.performative, result.performative)

        val content = message.content ?: message.contentObject.toString()
        Assert.assertThat("Content differs from expectations", content.trim(), CoreMatchers.anyOf(
            CoreMatchers.`is`(result.content.trim()),
            CoreMatchers.`is`(try { result.contentObject.toString().trim() } catch (_: UnreadableException) { "" })
        ))
        message.replyWith ?.let { Assert.assertEquals(it, result.inReplyTo) }
    }

    operator fun Int.plus(message: Any) = Message(this).apply { content = message.toString() }

    operator fun Message.minus(code: String) = this.apply { replyWith = code }

    operator fun Message.compareTo(receiver: Agent): Int {
        addReceiver(receiver.aid)
        senderAgent.send(this)
        return 0
    }
}