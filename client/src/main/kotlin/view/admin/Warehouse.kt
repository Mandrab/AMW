package view.admin

import common.ontology.dsl.abstraction.Item.Product
import view.utilities.Dialog
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Swing.button
import java.awt.GridBagLayout
import javax.swing.JPanel

class Warehouse(
    private val xLanesCount: Int,
    private val yLanesCount: Int
): JPanel() {
    private val items: Collection<Product> = emptyList()

    init {
        layout = GridBagLayout()

        (0 until xLanesCount).flatMap { x -> (0 until yLanesCount).map { y -> Pair(x, y) } }.forEach {
            add(button {
                text = "Rack ${it.first * yLanesCount + it.second + 1}"
                addActionListener { _ -> Dialog.Info(itemInfoString(it.second, it.first)) }
            }, constraint { gridx = it.second; gridy = it.first })
        }
    }

    fun refresh(): Nothing = TODO()

    private fun itemInfoString(rack: Int, shelf: Int) = items
            .find { it.positions.any { it.rack.id == rack && it.shelf.id == shelf } }
            ?.toString() ?: "No known element in this slot"
}