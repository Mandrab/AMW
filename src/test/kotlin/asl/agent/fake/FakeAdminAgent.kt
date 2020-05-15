package asl.agent.fake

import common.translation.LiteralBuilder
import common.translation.Service
import controller.agent.abstracts.TerminalAgent
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.Literal
import jason.asSyntax.StringTermImpl
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class FakeAdminAgent: TerminalAgent() {

	override fun setup() {
		super.setup()
		(arguments[0] as FakeAminProxy).setAgent(this)
	}

	fun executeButNotRespond(script: String, consumer: Function<ACLMessage?, Boolean>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		val scriptLiteral: Literal = LiteralBuilder("script").setValues(StringTermImpl(script))
			.setQueue(*emptySet<String>().map { StringTermImpl(it) }.toTypedArray()).build()
		val executeLiteral = LiteralBuilder("execute").setValues(scriptLiteral).build()

		MessageSender(Service.EXECUTOR_SCRIPT.service, Service.EXEC_SCRIPT.service, ACLMessage.CFP, executeLiteral).require(this)
			.thenAccept { result.complete(consumer.apply(it)) }
		return result;
	}

	fun waitMsg() = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE)) != null
}