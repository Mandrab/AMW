package asl.agent.fake

import common.translation.LiteralBuilder
import common.translation.Service.EXECUTOR_SCRIPT
import common.translation.ServiceType.EXEC_SCRIPT
import controller.agent.abstracts.TerminalAgent
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.Literal
import jason.asSyntax.StringTermImpl
import org.apache.tools.ant.taskdefs.optional.Script
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class FakeOrderManagerAgent: TerminalAgent() {

	override fun setup() {
		super.setup()
		(arguments[0] as FakeOrderManagerProxy).setAgent(this)
	}

	fun retrieve(message: String): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP, message).require(this)
			.thenAccept {
				it ?: result.complete(false)
				when (it?.performative) {
					ACLMessage.REFUSE -> result.complete(false)
					ACLMessage.PROPOSE -> {
						MessageSender(
							EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.ACCEPT_PROPOSAL,
							LiteralBuilder("retrieve").setValues("id").build()).setMsgID(it.inReplyTo).send(this)
						result.complete(true)
					}
				}
			}
		return result;
	}

	fun retrieveButNotRespond(script: String, consumer: Function<ACLMessage?, Boolean>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP,
			EXEC_SCRIPT.parse(Pair(script, emptySet<String>()))).require(this).thenAccept {
			result.complete(consumer.apply(it))
		}
		return result;
	}

	fun waitMsg() = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE)) != null
}