package controller.agent.client

import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import controller.agent.communication.LiteralParser.getValue
import controller.agent.communication.LiteralParser.split
import controller.agent.communication.Service.MANAGEMENT_ORDERS
import controller.agent.communication.ServiceType.ACCEPT_ORDER
import controller.agent.communication.ServiceType.INFO_ORDERS
import controller.agent.abstracts.ItemUpdater
import common.type.Order
import java.util.*
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
    private val client: String by lazy { arguments[1] as String }
    private val clientMail: String by lazy { arguments[2] as String }
    override val proxy: ClientProxy by lazy { arguments[0] as ClientProxy }
    override val updateTime: Long = 1000                            // update period time

    override fun setup() {
        super.setup()
        proxy.setAgent(this)
        addBehaviour(listenMessage())                               // add behaviour to listen for incoming messages
        addBehaviour(updateOrders())
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

    private fun updateOrders() = object: CyclicBehaviour() {
        private var lastUpdate: Long = 0

        override fun action() {                                     // update warehouse (items) info
            MessageSender(MANAGEMENT_ORDERS.service, INFO_ORDERS.service, ACLMessage.REQUEST,
                INFO_ORDERS.parse(arrayOf(client, clientMail))).require(agent).thenAccept { msg ->
                msg?.let { it ->
                        val id = getValue(it.content, "id")!!
                        val status = when (getValue(it.content, "status")!!) {
                            "checking_items" -> Order.Status.SUBMITTED
                            "checking_gather_point" -> Order.Status.ACCEPTED
                            "retrieving" -> Order.Status.ACCEPTED
                            "refused" -> Order.Status.REFUSED
                            "completed" -> Order.Status.COMPLETED
                            else -> Order.Status.SUBMITTED
                        }
                        val items = split(getValue(it.content, "items")!!)
                                .map { Pair(getValue(it, "id")!!, getValue(it, "quantity")!!.toInt()) }
                        proxy.dispatchOrder(Order(id, status, items))
                        TODO()
                    }
            }                                             // dispatch updated list of items
            lastUpdate = Date().time                                // update update-time

            while (Date().time - lastUpdate < updateTime)           // avoid exceptional wakeup
                block(updateTime)                                   // wait specified time before next update
        }
    }

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(client: String, email: String, address: String, vararg args: Pair<String, Int>) =
        MessageSender(MANAGEMENT_ORDERS.service, ACCEPT_ORDER.service, ACLMessage.REQUEST,
            ACCEPT_ORDER.parse(mutableListOf(client, email, address, args)).toString()).send(this)

    fun shutdown(): Future<Unit> {
        // TODO make behaviour to close things
        super.takeDown()
        return CompletableFuture.completedFuture(Unit)
    }
}