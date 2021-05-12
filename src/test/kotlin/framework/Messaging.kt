package framework

import jade.core.Agent
import jade.lang.acl.ACLMessage
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
        var mid: MID? = null
    }

    class MID { lateinit var value: String }

    operator fun Agent.rangeTo(message: Message) = message.apply {
        senderAgent = this@rangeTo
        sender = aid
        message.mid ?.let { inReplyTo = it.value }
    }

    operator fun Agent.compareTo(message: Message) = 0.apply {
        val result = blockingReceive(waitingTime)
        Assert.assertNotNull(result)
        Assert.assertEquals(message.performative, result.performative)
        message.content
            ?.let { Assert.assertEquals(it, result.content) }
            ?: Assert.assertEquals(message.contentObject.toString().trim(), result.content)
        message.replyWith ?.let { Assert.assertEquals(it, result.inReplyTo) }
        message.mid ?.let { it.value = result.inReplyTo }
    }

    operator fun Int.plus(message: Any) = Message(this).apply { content = message.toString() }

    operator fun Message.minus(code: String) = this.apply { replyWith = code }

    operator fun Message.minus(code: MID) = this.apply { mid = code }

    operator fun Message.compareTo(receiver: Agent): Int {
        addReceiver(receiver.aid)
        senderAgent.send(this)
        return 0
    }
}
