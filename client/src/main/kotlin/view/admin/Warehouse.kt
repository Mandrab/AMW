package view.admin

import common.ontology.dsl.abstraction.Item.Product
import view.utilities.Dialog
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Swing.button
import java.awt.GridBagLayout
import java.lang.StringBuilder
import javax.swing.JPanel

class Warehouse(
    private val xLanesCount: Int,
    private val yLanesCount: Int
): JPanel() {
    private var items: Collection<Product> = emptyList()

    init {
        layout = GridBagLayout()

        (0 until xLanesCount).flatMap { x -> (0 until yLanesCount).map { y -> Pair(x, y) } }.forEach {
            val rack = it.first * yLanesCount + it.second + 1
            add(button {
                text = "Rack $rack"
                addActionListener { Dialog.Info(itemInfoString(rack), "Info rack $rack") }
            }, constraint { gridx = it.second; gridy = it.first })
        }
    }

    fun refresh(elements: Collection<Product>) { items = elements }

    private fun itemInfoString(rack: Int) = items.filter { it.positions.any { it.rack.id == rack } }
            .fold(StringBuilder()) { acc1, item -> acc1.append(
                    item.positions.filter { it.rack.id == rack }.fold(StringBuilder()) { acc2, position ->
                        acc2.append(" Shelf: ${position.shelf.id}    Item: ${item.id.name} \n")
                    }
            ) }.let { if (it.isBlank()) "No known element in this slot" else it.toString() }
}