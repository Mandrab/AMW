package asl.agent.fake

import common.translation.LiteralBuilder
import common.translation.Service.MANAGEMENT_ITEMS
import common.translation.Service.EXECUTOR_SCRIPT
import common.translation.Service.PICKER_ITEMS
import common.translation.ServiceType.EXEC_SCRIPT
import common.translation.ServiceType.RETRIEVE_ITEMS
import common.translation.ServiceType.RETRIEVE_ITEM
import controller.agent.abstracts.TerminalAgent
import jade.lang.acl.ACLMessage
import jade.lang.acl.MessageTemplate
import jason.asSyntax.StringTermImpl
import jason.asSyntax.Structure
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class FakeOrderManagerAgent: TerminalAgent() {

	override fun setup() {
		super.setup()
		(arguments[0] as FakeOrderManagerProxy).setAgent(this)
	}

	fun retrieveItems(orderID: String, items: Collection<Pair<String, Int>>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(MANAGEMENT_ITEMS.service, RETRIEVE_ITEMS.service, ACLMessage.REQUEST,
			RETRIEVE_ITEMS.parse(Pair(orderID, items))).require(this).thenAccept {
				it ?: result.complete(false)
				when (it?.performative) {
					ACLMessage.FAILURE -> result.complete(false)
					ACLMessage.CONFIRM -> result.complete(true)
				}
			}
		return result
	}

	fun retrieveItem(message: String): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(PICKER_ITEMS.service, RETRIEVE_ITEM.service, ACLMessage.CFP, message).require(this)
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
		return result
	}

	fun retrieveButNotRespond(script: String, consumer: Function<ACLMessage?, Boolean>): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender(EXECUTOR_SCRIPT.service, EXEC_SCRIPT.service, ACLMessage.CFP,
			EXEC_SCRIPT.parse(Pair(script, emptySet<String>()))).require(this).thenAccept {
			result.complete(consumer.apply(it))
		}
		return result
	}

	fun waitMsg() = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.FAILURE)) != null

	fun getCollectionPoint(): CompletableFuture<Boolean> {
		val result = CompletableFuture<Boolean>()

		MessageSender("management(items)", "info(collection_points)", ACLMessage.CFP,
			Structure("point").addTerms(StringTermImpl("OrderXYZ"))).require(this) {
				it ?: result.complete(false)
				when (it?.performative) {
					ACLMessage.REFUSE -> result.complete(false)
					ACLMessage.PROPOSE -> {
						MessageSender("management(items)", "info(collection_points)",
							ACLMessage.ACCEPT_PROPOSAL, Structure("point").addTerms(StringTermImpl("OrderXYZ")))
							.setMsgID(it.inReplyTo).send(this)
						result.complete(true)
					}
				}
			}
		return result
	}
}