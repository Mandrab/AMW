package controller.agent.client

import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.Literal
import common.translation.LiteralBuilder
import common.translation.LiteralBuilder.Companion.pairTerm
import common.translation.LiteralParser.getValue
import common.translation.LiteralParser.split
import common.translation.Service.*
import controller.agent.abstracts.ItemUpdater
import common.type.Order
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class ClientAgent: ItemUpdater() {
    override val proxy: ClientProxy by lazy { arguments[0] as ClientProxy }
    override val updateTime: Long = 1000                            // update period time

    override fun setup() {
        super.setup()
        proxy.setAgent(this)
        addBehaviour(listenMessage())                               // add behaviour to listen for incoming messages
    }

    // TODO needed?
    private fun listenMessage() = object: CyclicBehaviour() {
        override fun action() {
            val message = receive(MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            ))

            if (message != null) {
                val content = message.content
                val struct1: String = split(getValue(content))[0]
                val struct2: String = split(getValue(content))[1]

                if (message.performative == ACLMessage.CONFIRM && content.startsWith("confirmation"))
                    proxy.dispatchOrder(
	                    Order(
		                    getValue(struct1),
		                    Order.Status.SUBMITTED,
		                    emptyList()
	                    )
                    )//, getValue(struct2)))
                else println("Received message: " + message.content) // TODO
            } else block()
        }
    }

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(vararg args: Pair<String, Int>): Future<Boolean> {
        val result = CompletableFuture<Boolean>()

        TODO()
        /*val l: MutableList<String> = mutableListOf(*args)
        val order: Literal = LiteralBuilder("order")
            .setValues(
                pairTerm("client", l.removeAt(0)),
                pairTerm("email", l.removeAt(0)),
                pairTerm("address", l.removeAt(0))
            ).setQueue(
                *l.groupBy { it }.entries.map {
                    LiteralBuilder("item")
                        .setValues(pairTerm("id", it.key), pairTerm("quantity", "" + it.value.size))
                        .build()
                }.toTypedArray()
            ).build()
        MessageSender(MANAGEMENT_ORDERS.toString(), ACCEPT_ORDER.toString(), ACLMessage.REQUEST, order).send(this)*/

        return result
    }

    fun shutdown(): Future<Unit> {
        // TODO make behaviour to close things
        super.takeDown()
        return CompletableFuture.completedFuture(Unit)
    }
}