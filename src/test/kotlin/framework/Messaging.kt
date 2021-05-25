package framework

import jade.core.Agent
import jade.lang.acl.ACLMessage
import jade.lang.acl.UnreadableException
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

    var lastMatches = listOf<String>()

    class Message(performative: Int): ACLMessage(performative) {
        lateinit var senderAgent: Agent
        lateinit var objectContent: Any
    }

    operator fun Agent.rangeTo(message: Message) = message.apply { senderAgent = this@rangeTo; sender = aid }

    operator fun Agent.compareTo(message: Message) = 0.apply {
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull("An expected message has not arrived", result)
        Assert.assertEquals("Performative differs from expectations", message.performative, result.performative)

        val regex = reverseRegex(message.content ?: message.contentObject.toString().trim())
        val content1 = result.content.trim()
        val content2 = try { result.contentObject.toString().trim() } catch (_: UnreadableException) { "" }
        Assert.assertTrue(
            "Content differs from expectations:\nExpected: $regex\nBut was: ${result.content}",
            content1.matches(regex.toRegex()) || content2.matches(regex.toRegex())
        )
        lastMatches = regex.toRegex().find(content1)?.groupValues?.drop(1)
            ?: regex.toRegex().find(content2)?.groupValues?.drop(1)
            ?: emptyList()

        message.replyWith ?.let { Assert.assertEquals(it, result.inReplyTo) }
    }

    operator fun Int.plus(message: Any) = Message(this).apply {
        objectContent = message
        content = message.toString()
    }

    operator fun Message.minus(code: String) = this.apply { replyWith = code }

    operator fun Message.compareTo(receiver: Agent): Int {
        addReceiver(receiver.aid)
        senderAgent.send(this)
        return 0
    }

    private fun reverseRegex(string: String) = listOf("*", "+", "?", ".", "^", "[", "]", "(", ")", "$", "&", "|")
        .foldRight(string) { symbol: String, accumulator: String ->
            accumulator.replace(symbol, """\$symbol""")             // abc?def --> abc\?def
                .replace("""\\$symbol""", symbol)                   // abc\\?def --> abc?def
        }
}
