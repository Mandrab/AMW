package controller.user.agent

import controller.agent.abstracts.ItemUpdater

/**
 * Agent for client-side application.
 * It allows to communicate to obtain items information,
 * additionally, allow to obtain order information
 *
 * @author Paolo Baldini
 */
class Agent: ItemUpdater() {
    private val client: String by lazy { arguments[1] as String }
    private val clientMail: String by lazy { arguments[2] as String }

    override fun setup() {
        super.setup()
        proxy.setAgent(this)
    }

    /**
     * Allows to place an order with submitted elements
     */
    fun placeOrder() { TODO() }

    fun shutdown() = super.takeDown()
}