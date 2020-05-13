package view.panel

import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.GridBagLayout
import java.util.Vector
import javax.swing.*
import common.type.Item
import io.reactivex.rxjava3.functions.Consumer
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * A panel structured for allow an administrator to see items in warehouse and,
 * in the future, to add more items to this.
 *
 * TODO allow (at least) to increment items quantity
 *
 * @author Paolo Baldini
 */
open class AddItem: JPanel(), Consumer<Collection<Item>> {
	private var items = mutableListOf<Item>()
	private var itemsList = ComponentsBuilder.createList(Vector(items.map { it.itemId }),
		items.size.coerceAtMost(25), 225)

	private val itemID = JTextArea("ID: -").also { it.isEditable = false }
	private val itemReserved = JTextArea("Reserved: -").also { it.isEditable = false }
	private val itemsPositionsList = ComponentsBuilder.createList(Vector(),
		items.size.coerceAtMost(25), 225)

	init {
		layout = GridBagLayout()

		GridBagPanelAdder().xPos(1).weight(.33, .0).addTo(this, itemID)

		GridBagPanelAdder().xPos(2).weight(.33, .0).addTo(this, itemReserved)

		val itemsPosPane = JScrollPane(itemsPositionsList)
		GridBagPanelAdder().position(1, 1).weight(0.66, 1.0).xWide(2).addTo(this, itemsPosPane)

		val itemsPane = JScrollPane(itemsList)
		itemsList.addMouseListener(object: MouseAdapter() {
			override fun mouseClicked(e: MouseEvent) {
				if (items.isNotEmpty()) {
					val elem = items.elementAt(itemsList.selectedIndex)
					itemID.text = "ID: " + elem.itemId
					itemReserved.text = "Reserved: " + elem.reserved.toString()
					itemsPositionsList.setListData(Vector(items.filter { it.itemId == elem.itemId }
						.flatMap { it.positions }.map { "rack: ${it.first},    shelf: ${it.second},    quantity: ${it.third}" }))
				}
			}
		})
		GridBagPanelAdder().weight(.33, 1.0).yWide(2).addTo(this, itemsPane)
	}

	override fun accept(items: Collection<Item>) {
		this.items = items.toMutableList()
		itemsList.setListData(Vector(this.items.map { it.itemId }))
	}
}