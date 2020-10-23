package view.panel

import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.Dimension
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagLayout
import java.util.Vector
import javax.swing.*
import common.type.Item
import common.Request.PLACE_ORDER
import io.reactivex.rxjava3.functions.Consumer
import view.ViewImpl

/**
 * A panel structured for allow a client to submit a new order.
 * Beeing that only a project-application, the purchase is not performed
 *
 * @author Paolo Baldini
 */
open class NewOrder: JPanel(), Consumer<Collection<Item>> {
	private var items = mutableListOf<ItemPair>()
	private val itemsList = ComponentsBuilder.createList(Vector(items.map { "${it.first}        ${it.second}" }),
		items.size.coerceAtMost(25), 225)
	private val selectedItems: Vector<ItemPair> = Vector()     // selected items lists
	private val selectedItemsList = ComponentsBuilder.createList(Vector(selectedItems.map { "${it.first}        ${it.second}" }), 10, 225)

	init {
		// window panel setup
		layout = GridBagLayout()

		// client name
		GridBagPanelAdder().xPos(1).west(10).xWeight(.33).fill(HORIZONTAL).addTo(this, JTextArea("Client").also { it.isEditable = false })
		val clientInput = JTextField("name", 20)
		GridBagPanelAdder().xPos(2).fill(HORIZONTAL).addTo(this, clientInput)

		// client mail
		GridBagPanelAdder().position(1, 1).west(10).xWeight(.33).fill(HORIZONTAL)
			.addTo(this, JTextArea("Email").also { it.isEditable = false })
		val mailInput = JTextField("mail@mail.com", 20)
		GridBagPanelAdder().position(2, 1).fill(HORIZONTAL).addTo(this, mailInput)

		// client address
		GridBagPanelAdder().position(1, 2).west(10).xWeight(.33).fill(HORIZONTAL)
			.addTo(this, JTextArea("Address").also { it.isEditable = false })
		val addressInput = JTextField("address", 20);
		GridBagPanelAdder().position(2, 2).fill(HORIZONTAL).addTo(this, addressInput)

		// submit
		val submitButton = JButton("Submit");
		submitButton.addActionListener {
			if (selectedItems.size > 0) {
				val l = ArrayList<Any>()
				l.add(clientInput.text)
				l.add(mailInput.text)
				l.add(addressInput.text)
				l.addAll(selectedItems.elements().toList().map { Pair(it.first, it.second) })
				ViewImpl.publish(PLACE_ORDER, *l.toTypedArray())
				// TODO
			}
		}
		GridBagPanelAdder().position(2, 7).fill(HORIZONTAL).addTo(this, submitButton)

		// selected items list
		GridBagPanelAdder().position(1, 3).west(10).fill(HORIZONTAL).addTo(this, JTextArea("Items").also { it.isEditable = false })
		GridBagPanelAdder().position(2, 3).weight(0.33, 1.0).addTo(this, JScrollPane(selectedItemsList))
		val removeButton = JButton("<-")
		removeButton.addActionListener{
			if (selectedItems.isNotEmpty() && !selectedItemsList.isSelectionEmpty) {
				val selElem = selectedItems.elementAt(selectedItemsList.selectedIndex)
				selectedItems.remove(selElem)
				if (selElem.second > 1) selectedItems.add(ItemPair(selElem.first, selElem.second -1))
				selectedItemsList.setListData(Vector(selectedItems.map { "${it.first}        Quantity: ${it.second}" }))

				val elem = items.find { it.first == selElem.first }
				items.remove(elem)
				val pair = elem?.run { ItemPair(elem.first, elem.second +1) } ?: ItemPair(selElem.first, 1)
				items.add(pair)
				itemsList.setListData(Vector(items.map { "${it.first}        Quantity: ${it.second}" }))
			}
		}
		GridBagPanelAdder().position(2, 6).fill(HORIZONTAL).addTo(this, removeButton)

		// store list
		itemsList.minimumSize = Dimension(50, 50)
		GridBagPanelAdder().wideness(1, 4).weight(0.33, 1.0).addTo(this, JScrollPane(itemsList))
		val addButton = JButton("->")
		addButton.addActionListener{
			if (items.isNotEmpty() && !itemsList.isSelectionEmpty) {
				val elem = items.elementAt(itemsList.selectedIndex)
				items.remove(elem)
				if (elem.second > 1) items.add(ItemPair(elem.first, elem.second -1))
				itemsList.setListData(Vector(items.map { "${it.first}        Quantity: ${it.second}" }))

				val selElem = selectedItems.find { it.first == elem.first }
				selectedItems.remove(selElem)
				val pair = selElem?.run { ItemPair(selElem.first, selElem.second +1) } ?: ItemPair(elem.first, 1)
				selectedItems.add(pair)
				selectedItemsList.setListData(Vector(selectedItems.map { "${it.first}        Quantity: ${it.second}" }))
			}
		}
		GridBagPanelAdder().yPos(6).fill(HORIZONTAL).addTo(this, addButton);
	}

	@Synchronized override fun accept(t: Collection<Item>) {
		val _items: MutableList<ItemPair> = t.groupBy({ it.itemId }, { it -> it.positions.map { it.third }
			.sum() - it.reserved }).map { ItemPair(it.key, it.value.sum()) }.filter { it.second > 0 }.toMutableList()

		var update = selectedItems.removeIf { !_items.contains(it) }                    // remove no more present items
		update = update || items.removeIf { !_items.contains(it) }                      // remove no more present items
		update = update || _items.filter { !items.contains(it) }.filter {
			val elem = selectedItems.find { i -> i.first == it.first }
			elem?.let {
				it.second - elem.second > 0
			} ?: true
		}.onEach { items.add(it) }.any()

		items.replaceAll { it ->
			val selElem = selectedItems.find { i -> i.first == it.first }
			var newElem = _items.find { i -> i.first == it.first }!!

			selElem?.let {
				newElem = ItemPair(it.first, newElem.second - selElem.second)
			}
			if (it.second != newElem.second) update = true

			newElem
		}
		items.filter { it.second < 0 }.forEach {
			val elem = selectedItems.find { i -> i.first == it.first }
			elem?.let {
				selectedItems.remove(elem)
				if (elem.second - it.second > 0)
					selectedItems.add(ItemPair(elem.first, elem.second - it.second))
				update = true
			}
		}
		items.removeAll { it.second <= 0 }

		if (update) itemsList.setListData(Vector(items.map { "${it.first}        Quantity: ${it.second}" }))
		if (update) selectedItemsList.setListData(Vector(selectedItems.map { "${it.first}        Quantity: ${it.second}" }))
	}

	data class ItemPair(val first: String, val second: Int) {
		override fun equals(other: Any?): Boolean = other is ItemPair && first == other.first
		override fun hashCode(): Int = first.hashCode()
	}
}