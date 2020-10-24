package controller.user.agent

import common.Item.ShopItem
import common.User.User
import controller.agent.communication.Messages.message
import controller.agent.communication.Messages.receiver
import controller.agent.communication.ontology.Ontology.AcceptOrder
import controller.user.agent.Proxy.Proxy
import jade.lang.acl.ACLMessage.*
import jade.core.Agent as JadeAgent

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class Agent: JadeAgent() {

    override fun setup() {
        super.setup()
        (arguments[0] as Proxy).setAgent(this)
    }

    fun shutdown() = takeDown()

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder(user: User, elements: List<ShopItem>) = send(
        message {
            performative = REQUEST
            payloadObject = AcceptOrder.parse(user, elements)
            receivers {
                receiver {
                    supplier = AcceptOrder.serviceSupplier
                    service = AcceptOrder.serviceType
                }
            }
        }
    )
}