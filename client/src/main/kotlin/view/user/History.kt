package view.user

import common.ontology.dsl.operation.Order.Order
import view.utilities.swing.Grid.constraint
import view.utilities.swing.List.list
import java.awt.GridBagLayout
import java.util.*
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel

class History(
    private val ordersSupplier: () -> Collection<Order>
): JPanel() {
    private val orders: JList<Order>

    init {
        layout = GridBagLayout()

        val orderInfo = JLabel("Select an order")
        add(orderInfo, constraint { gridx = 1 })

        orders = list {
            elements = ordersSupplier()
            onClick = { orderInfo.text = it.toString() }
        }
        add(orders, constraint { })
    }

    fun refresh() = orders.setListData(Vector(ordersSupplier()))
}