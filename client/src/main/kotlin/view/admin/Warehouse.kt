package view.admin

import common.ontology.dsl.abstraction.ID.id
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
    private val addItem: (item: WarehouseItem) -> Unit
): JPanel() {
    private var items: Collection<Product> = emptyList()

    init {
        layout = GridBagLayout()

        (0 until xLanesCount).flatMap { x -> (0 until yLanesCount).map { y -> Pair(x, y) } }.forEach {
            val rack = it.first * yLanesCount + it.second + 1
            add(button {
                text = "Rack $rack"
                addActionListener { Dialog.Info(itemInfoString(rack), "Info rack $rack") }
            }, constraint { gridx = it.second; gridy = it.first + 1 })
        }

        val idIn = JTextArea("id")
        val rackIn = spinner(1, 1, xLanesCount)
        val shelfIn = spinner(1, 1, yLanesCount)
        val quantityIn = spinner(1, 1, 100)
        add(descriptionLabel("Item ID: ", idIn), constraint { gridx = 0; gridy = 0 })
        add(descriptionLabel("rack: ", rackIn), constraint { gridx = 1; gridy = 0 })
        add(descriptionLabel("shelf: ", shelfIn), constraint { gridx = 2; gridy = 0 })
        add(descriptionLabel("quantity: ", quantityIn), constraint { gridx = 3; gridy = 0 })
        add(button {
            text = "add item"
            addActionListener {
                addItem(item(id(idIn.text), position(
                        rack(rackIn.value as Int),
                        shelf(shelfIn.value as Int),
                        quantity(quantityIn.value as Int))))
            }
        }, constraint { gridx = 4; gridy = 0 })
    }

    fun refresh(elements: Collection<Product>) { items = elements }

    private fun itemInfoString(rack: Int) = items.filter { it.positions.any { it.rack.id == rack } }
            .fold(StringBuilder()) { acc1, item -> acc1.append(
                    item.positions.filter { it.rack.id == rack }.fold(StringBuilder()) { acc2, position ->
                        acc2.append(" Shelf: ${position.shelf.id}    Item: ${item.id.name} \n")
                    }
            ) }.let { if (it.isBlank()) "No known element in this slot" else it.toString() }
}