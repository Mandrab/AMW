package view.user

import common.ontology.dsl.operation.Order.PlaceOrder
import view.utilities.swing.Grid.constraint
import view.utilities.swing.List.List
import view.utilities.swing.List.list
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel

class History: JPanel() {
    private val orders: List<PlaceOrder>

    init {
        layout = GridBagLayout()

        val orderInfo = JLabel("Select an order")
        add(orderInfo, constraint { gridx = 1 })

        orders = list {
            elements = emptyList()
            onClick = { orderInfo.text = it.toString() }
        }
        add(orders, constraint { })
    }

    fun refresh(elements: Collection<PlaceOrder>) { orders.elements = elements }
}