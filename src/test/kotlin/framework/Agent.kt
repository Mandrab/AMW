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

    class Message(performative: Int): ACLMessage(performative) {
        lateinit var senderAgent: Agent
    }

    operator fun Agent.rangeTo(message: Message) = message.apply { senderAgent = this@rangeTo; sender = aid }

    operator fun Agent.compareTo(message: Message) = 0.apply {
        val result = blockingReceive()
        Assert.assertEquals(message.performative, result.performative)
        Assert.assertEquals(message.content, result.content)
        message.replyWith ?.let { Assert.assertEquals(it, result.replyWith) }
    }

    operator fun Int.plus(message: Any) = Message(this).apply { content = message.toString() }

    operator fun Message.minus(code: String) = this.apply { replyWith = code }

    operator fun Message.compareTo(receiver: Agent): Int {
        addReceiver(receiver.aid)
        senderAgent.send(this)
        return 0
    }
}
