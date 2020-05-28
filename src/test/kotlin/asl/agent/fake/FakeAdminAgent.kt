package asl.agent.fake

import common.translation.Service
import common.translation.Service.EXECUTOR_SCRIPT
import common.translation.ServiceType
import common.translation.ServiceType.EXEC_SCRIPT
import controller.agent.abstracts.TerminalAgent
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.NumberTermImpl
import jason.asSyntax.StringTermImpl
import jason.asSyntax.Structure
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class FakeAdminAgent: TerminalAgent() {

	override fun setup() {
		super.setup()
		(arguments[0] as FakeAdminProxy).setAgent(this)
	}

	fun getCommand(id: String, consumer: Function<ACLMessage?, Unit>) {
		MessageSender(Service.MANAGEMENT_COMMANDS.service, "request(command)", ACLMessage.REQUEST,
			Structure("request").addTerms(Structure("command_id").addTerms(StringTermImpl(id)))).require(this)
			.thenAccept { consumer.apply(it) }
	}

	fun removeReserved(id: String, rack: Int, shelf: Int, quantity: Int): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		val msg = Structure("remove_reserved").addTerms(Structure("item").addTerms(
			Structure("id").addTerms(StringTermImpl(id)),
			Structure("position").addTerms(
				Structure("rack").addTerms(NumberTermImpl(rack.toDouble())),
				Structure("shelf").addTerms(NumberTermImpl(shelf.toDouble())),
				Structure("quantity").addTerms(NumberTermImpl(quantity.toDouble()))
			)
		))

		MessageSender(Service.MANAGEMENT_ITEMS.service, ServiceType.REMOVE_ITEM.service, ACLMessage.REQUEST, msg).require(this)
			.thenAccept {
				it ?. apply {
					when (it!!.performative) {
						ACLMessage.FAILURE -> result.complete(false)
						ACLMessage.CONFIRM -> println(it).also { result.complete(true) }
					}
				} ?: let { result.complete(false) }
			}

		return result
	}

	fun executeButNotRespond(script: String, consumer: Function<ACLMessage?, Boolean>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP,
			EXEC_SCRIPT.parse(Pair(script, emptySet<String>()))).require(this)
			.thenAccept { result.complete(consumer.apply(it)) }
		return result
	}

	fun waitMsg() = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE)).also { println(it) } != null
}