package view.user

import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Item.sameId
import common.ontology.dsl.abstraction.Quantity.quantity
import view.utilities.swing.Grid.constraint
import view.utilities.swing.List.list
import view.utilities.swing.List.List
import view.utilities.swing.List.clean
import view.utilities.swing.List.map
import view.utilities.swing.List.minusAssign
import view.utilities.swing.List.plusAssign
import view.utilities.swing.List.render
import view.utilities.swing.Swing.button
import view.utilities.swing.Swing.label
import java.awt.GridBagLayout
import java.util.*
import javax.swing.JPanel

class Shop(
    private val itemsSupplier: () -> Collection<QuantityItem>,
    private val placeOrder: (items: Collection<QuantityItem>) -> Unit
): JPanel() {
    private val items: List<QuantityItem>
    private val selectedItems: List<QuantityItem>

    init {
        layout = GridBagLayout()

        add(label { text = "Shop items" })
        items = list {
            elements = itemsSupplier()
            cellRenderer = render { " ID: ${it.id.name}    Quantity: ${it.quantity.value} " }
        }
        add(items, constraint { gridy = 1; gridheight = 4 })

        add(label { text = "Chart items" }, constraint { gridx = 2 })
        selectedItems = list {
            cellRenderer = render { " ID: ${it.id.name}    Quantity: ${it.quantity.value} " }
        }
        add(selectedItems, constraint { gridx = 2; gridy = 1; gridheight = 4 })

        add(button {
            text = ">"
            addActionListener { items.selectedValue?.let { moveItem(items, selectedItems, it) } }
        }, constraint { gridx = 1; gridy = 2 })
        add(button {
            text = "<"
            addActionListener { selectedItems.selectedValue?.let { moveItem(selectedItems, items, it) } }
        }, constraint { gridx = 1; gridy = 3 })
        add(button {
            text = "Order"
            addActionListener { placeOrder(selectedItems.elements) }
        }, constraint { gridx = 2; gridy = 5 })
    }

    fun refresh() {
        items.setListData(Vector(itemsSupplier()))
        selectedItems.clean()
    }

    private fun moveItem(from: List<QuantityItem>, to: List<QuantityItem>, item: QuantityItem) {
        from -= item
        if (item.quantity.value > 1) from += item(item.id, quantity(item.quantity.value - 1))

        if (to.elements.none { item sameId it }) to += item(item.id, quantity(1))
        else to.map({ item sameId it }, { item(it.id, quantity(it.quantity.value + 1)) })
    }
}
