package asl.agent.fake

import common.translation.Service
import common.translation.Service.EXECUTOR_SCRIPT
import common.translation.ServiceType.EXEC_SCRIPT
import controller.agent.abstracts.TerminalAgent
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.Atom
import jason.asSyntax.StringTermImpl
import jason.asSyntax.Structure
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class FakeAdminAgent: TerminalAgent() {

	override fun setup() {
		super.setup()
		(arguments[0] as FakeAminProxy).setAgent(this)
	}

	fun getCommand(id: String, consumer: Function<ACLMessage?, Unit>) {
		MessageSender(Service.MANAGEMENT_COMMANDS.service, "request(command)", ACLMessage.REQUEST,
			Structure("request").addTerms(Structure("command_id").addTerms(StringTermImpl(id)))).require(this)
			.thenAccept { consumer.apply(it) }
	}

	fun executeButNotRespond(script: String, consumer: Function<ACLMessage?, Boolean>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP,
			EXEC_SCRIPT.parse(Pair(script, emptySet<String>()))).require(this)
			.thenAccept { result.complete(consumer.apply(it)) }
		return result
	}

	fun waitMsg() = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE)) != null
}