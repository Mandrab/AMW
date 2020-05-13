package view.panel

import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.Dimension
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagLayout
import java.util.Vector
import javax.swing.*
import common.type.Item
import common.RequestDispatcherImpl
import common.Request.ORDER
import io.reactivex.rxjava3.functions.Consumer

/**
 * A panel structured for allow a client to submit a new order.
 * Beeing that only a project-application, the purchase is not performed
 *
 * @author Paolo Baldini
 */
open class NewOrder: JPanel(), Consumer<Collection<Item>> {
	private var items = mutableSetOf<Pair<String, Int>>()
	private var itemsList = ComponentsBuilder.createList(Vector(items.map { "${it.first}        ${it.second}" }),
		items.size.coerceAtMost(25), 225)

	init {
		// window panel setup
		layout = GridBagLayout()

		// selected items lists
		val selectedItems: Vector<Pair<String, Int>> = Vector()

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
				l.addAll(selectedItems.elements().toList())
				RequestDispatcherImpl.dispatch(ORDER, *l.toTypedArray())
				// TODO
			}
		}
		GridBagPanelAdder().position(2, 7).fill(HORIZONTAL).addTo(this, submitButton)

		// selected items list
		GridBagPanelAdder().position(1, 3).west(10).fill(HORIZONTAL).addTo(this, JTextArea("Items").also { it.isEditable = false })
		val selectedItemsList = ComponentsBuilder.createList(Vector(selectedItems.map { "${it.first}        ${it.second}" }), 10, 225)
		GridBagPanelAdder().position(2, 3).weight(0.33, 1.0).addTo(this, JScrollPane(selectedItemsList))
		val removeButton = JButton("<-")
		removeButton.addActionListener{
			if (selectedItems.isNotEmpty() && !selectedItemsList.isSelectionEmpty) {
				val selElem = selectedItems.elementAt(selectedItemsList.selectedIndex)
				selectedItems.remove(selElem)
				if (selElem.second > 1) selectedItems.add(Pair(selElem.first, selElem.second -1))
				selectedItemsList.setListData(Vector(selectedItems.map { "${it.first}        Quantity: ${it.second}" }))

				val elem = items.find { it.first == selElem.first }
				items.remove(elem)
				val pair = elem?.run { Pair(elem.first, elem.second +1) } ?: Pair(selElem.first, 1)
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
				if (elem.second > 1) items.add(Pair(elem.first, elem.second -1))
				itemsList.setListData(Vector(items.map { "${it.first}        Quantity: ${it.second}" }))

				val selElem = selectedItems.find { it.first == elem.first }
				selectedItems.remove(selElem)
				val pair = selElem?.run { Pair(selElem.first, selElem.second +1) } ?: Pair(elem.first, 1)
				selectedItems.add(pair)
				selectedItemsList.setListData(Vector(selectedItems.map { "${it.first}        Quantity: ${it.second}" }))
			}
		}
		GridBagPanelAdder().yPos(6).fill(HORIZONTAL).addTo(this, addButton);
	}

	override fun accept(items: Collection<Item>) {
		this.items = items.groupBy({ it.itemId }, { it.positions.map { it.third }.sum() - it.reserved })
			.map { Pair(it.key, it.value.sum()) }.filter { it.second > 0 }.toMutableSet()
		itemsList.setListData(Vector(this.items.map { "${it.first}        Quantity: ${it.second}" }))
	}
}