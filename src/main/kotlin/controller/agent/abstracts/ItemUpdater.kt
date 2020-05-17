package controller.agent.abstracts

import common.translation.LiteralParser
import common.translation.Service
import common.translation.ServiceType.INFO_WAREHOUSE
import common.type.Item
import jade.core.behaviours.CyclicBehaviour
import jade.lang.acl.ACLMessage
import jason.asSyntax.Structure
import java.util.*

/**
 * Agent to update items list.
 * In the application however, it's only extended.
 *
 * @author Paolo Baldini
 */
open class ItemUpdater: TerminalAgent() {
	private var lastUpdate: Long = 0                                // last items update

	open val proxy: ItemUpdaterProxy by lazy { arguments[0] as ItemUpdaterProxy }
	open val updateTime: Long = 5000                                // update period time

	/**
	 * Add update items behaviour
 	 */
	override fun setup() = apply { super.setup() }.addBehaviour(updateItems())

	private fun updateItems() = object: CyclicBehaviour() {
		override fun action() {                                     // update warehouse (items) info
			MessageSender(Service.MANAGEMENT_ITEMS.service, INFO_WAREHOUSE.service, ACLMessage.CFP, INFO_WAREHOUSE.literal)
				.require(agent).thenAccept { message ->
					proxy.dispatchItems(LiteralParser.split(message!!.content).map { Item.parse(it) }
						.groupBy { it.itemId }.entries.map{
							Item(it.key, it.value[0].reserved, it.value.map { i -> i.positions }.flatten())
						}
					) }                                             // dispatch updated list of items
			lastUpdate = Date().time                                // update update-time

			while (Date().time - lastUpdate < updateTime)           // avoid exceptional wakeup
				block(updateTime)                                   // wait specified time before next update
		}
	}
}