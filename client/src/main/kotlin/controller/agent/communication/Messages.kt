package controller.agent.communication

import jade.core.AID
import jade.lang.acl.ACLMessage

object Messages {

    fun message(init: ACLMessage.() -> Unit): ACLMessage = ACLMessage().apply(init)

    fun ACLMessage.receivers(block: AID.() -> Unit) = addReceiver(AID().apply(block))
}