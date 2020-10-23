package controller.user.agent

import controller.agent.AgentProxy

object Proxy {

    interface Proxy: AgentProxy {

        fun placeOrder()

        fun shutdown()
    }

    operator fun invoke(): Proxy = ClientProxy()
}