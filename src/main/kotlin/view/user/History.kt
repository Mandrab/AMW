package view.user

import common.ontology.dsl.operation.Order.InfoOrder
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Label.label
import view.utilities.swing.List.List
import view.utilities.swing.List.list
import view.utilities.swing.List.render
import java.awt.GridBagLayout
import javax.swing.JPanel

class History: JPanel() {
    private val orders: List<InfoOrder>

    init {
        layout = GridBagLayout()

        val orderInfo = label { text = "Select an order" }
        add(orderInfo, constraint { gridx = 1 })

        orders = list {
            elements = emptyList()
            onClick = { orderInfo.text = it.toString() }
            cellRenderer = render { " ID: ${it.id.name}    Status: ${it.status.state} " }
        }
        add(orders, constraint { })
    }

    fun refresh(elements: Collection<InfoOrder>) { orders.elements = elements }
}