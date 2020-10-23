package controller.agent

import jade.core.Agent
import jade.core.behaviours.Behaviour
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.OneShotBehaviour
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate

object AgentUtils {

    fun oneShotBehaviour(operation: (behaviour: Behaviour) -> Unit) = object: OneShotBehaviour() {
        override fun action() = operation(this)
    }

    fun cyclicBehaviour(operation: (behaviour: Behaviour) -> Unit) = object: CyclicBehaviour() {
        override fun action() = operation(this)
    }

    fun Agent.receiveId(id: String): ACLMessage? = receive(MessageTemplate.MatchInReplyTo(id))

    fun Agent.receiveContent(content: String): ACLMessage? = receive(MessageTemplate.MatchContent(content))

    fun ACLMessage.matchID(pattern: String) = MessageTemplate.MatchInReplyTo(pattern).match(this)

    fun ACLMessage.matchContent(pattern: String) = MessageTemplate.MatchContent(pattern).match(this)
}