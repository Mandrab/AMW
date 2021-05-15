package view.admin

import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Shelf.shelf
import view.utilities.Dialog
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Label.descriptionLabel
import view.utilities.swing.Swing.button
import view.utilities.swing.Spinner.spinner
import java.awt.GridBagLayout
import java.lang.StringBuilder
import javax.swing.JPanel
import javax.swing.JTextArea

class Warehouse(
    private val xLanesCount: Int,
    private val yLanesCount: Int,
    private val addItem: (item: WarehouseItem) -> Unit,
    private val removeItem: (item: QuantityItem) -> Unit
): JPanel() {
    private var items: Collection<Product> = emptyList()

    init {
        layout = GridBagLayout()

        (0 until xLanesCount).flatMap { x -> (0 until yLanesCount).map { y -> Pair(x, y) } }.forEach {
            val rack = it.first * yLanesCount + it.second + 1
            add(button {
                text = "Rack $rack"
                addActionListener { Dialog.Info(itemInfoString(rack), "Info rack $rack") }
            }, constraint { gridx = it.second; gridy = it.first + 2 })
        }

        val idIn = JTextArea("id")
        val quantityIn = spinner(1, 1, 100)
        val rackIn = spinner(1, 1, xLanesCount * yLanesCount)
        val shelfIn = spinner(1, 1, 100)
        add(descriptionLabel("Item ID: ", idIn), constraint { gridx = 0; gridy = 0; gridwidth = 2 })
        add(descriptionLabel("quantity: ", quantityIn), constraint { gridx = 2; gridy = 0; gridwidth = 2 })
        add(descriptionLabel("rack: ", rackIn), constraint { gridx = 0; gridy = 1; gridwidth = 2 })
        add(descriptionLabel("shelf: ", shelfIn), constraint { gridx = 2; gridy = 1; gridwidth = 2 })
        add(button { text = "add item"
            addActionListener {
                addItem(item(id(idIn.text), position(
                        rack(rackIn.value as Int),
                        shelf(shelfIn.value as Int),
                        quantity(quantityIn.value as Int)))
                )
            }
        }, constraint { gridx = 4; gridy = 0 })
        add(button { text = "remove item"
            addActionListener { removeItem(item(id(idIn.text), quantity(quantityIn.value as Int))) }
        }, constraint { gridx = 4; gridy = 1 })
    }

    fun refresh(elements: Collection<Product>) { items = elements }

    private fun itemInfoString(rack: Int) =
        items.flatMap { item -> item.positions.map { item to it } }
            .filter { it.second.rack.id == rack }
            .fold(StringBuilder()) { acc, (item, position) -> acc.append(
                " Shelf: ${position.shelf.id}   Item: ${item.id.name}   Quantity: ${position.quantity.value} \n"
            ) }
            .let { if (it.isBlank()) "No known element in this slot" else it.toString() }
}
