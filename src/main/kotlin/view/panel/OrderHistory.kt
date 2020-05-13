package view.panel

import io.reactivex.rxjava3.functions.Consumer
import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.Dimension
import java.awt.GridBagLayout
import java.util.Vector
import javax.swing.*
import model.Order
import java.awt.event.*

/**
 * A panel structured for allow to see old orders made by an user
 *
 * @author Paolo Baldini
 */
open class OrderHistory: JPanel(), Consumer<Collection<Order>> {
	private var orders: Set<Order> = emptySet()

	private val orderID = JTextArea("Order ID: -")
	private val orderStatus = JTextArea("Status: -")
	private val ordersList = ComponentsBuilder.createList(Vector(), orders.size.coerceAtMost(25), 225)
	private val itemsList = ComponentsBuilder.createList(Vector(), orders.size.coerceAtMost(25), 225)

	init {
        layout = GridBagLayout()

		orderID.isEditable = false
		orderStatus.isEditable = false
		ordersList.minimumSize = Dimension(50, 50)
		itemsList.minimumSize = Dimension(50, 50)

		val ordersPane = JScrollPane(ordersList)
		ordersList.addMouseListener(object: MouseAdapter() {
			override fun mouseClicked(e: MouseEvent) {
				if (orders.isNotEmpty()) {
					val elem = orders.elementAt(ordersList.selectedIndex)
					orderID.text = "Order ID: " + elem.id
					orderStatus.text = "Status: " + elem.status.name
					itemsList.setListData(Vector(elem.items.map { "ID: ${it.first}         Quantity: ${it.second}" }))
				}
			}
		})
		GridBagPanelAdder().xWeight(0.33).yWide(3).addTo(this, ordersPane)

		GridBagPanelAdder().xPos(1).weight(0.33, .0).addTo(this, orderID)

		GridBagPanelAdder().xPos(2).weight(0.33, .0).addTo(this, orderStatus)

		val itemsPane = JScrollPane(itemsList)
		GridBagPanelAdder().position(1, 1).weight(0.66, 1.0).xWide(2).addTo(this, itemsPane)
	}

	override fun accept(items: Collection<Order>) {
		orders = items.toSet()
		ordersList.setListData(Vector(orders.map { it.id }))
	}
}