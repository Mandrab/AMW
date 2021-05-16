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
    private val orderInfo = label { text = "Nothing here. You can try to refresh the view" }

    init {
        layout = GridBagLayout()

        add(orderInfo, )

        orders = list {
            elements = emptyList()
            cellRenderer = render { " ID: ${it.id.name}    Status: ${it.status.state} " }
        }
        add(orders, constraint { })
    }

    fun refresh(elements: Collection<InfoOrder>) {
        orders.elements = elements
        if (elements.isNotEmpty()) remove(orderInfo)
        repaint()
    }
}
