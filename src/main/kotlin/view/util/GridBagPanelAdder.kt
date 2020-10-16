package view.util

import java.awt.Component
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.BOTH
import java.awt.GridBagConstraints.NORTHEAST
import java.awt.Insets

/**
 * An utility builder to comfortably locate component using GridBagLayout
 *
 * @author Paolo Baldini
 */
class GridBagPanelAdder {
    private var gridX = 0
    private var gridY = 0
    private var wideX = 1
    private var wideY = 1
    private var north = 0
    private var east = 0
    private var south = 0
    private var west = 0
    private var weightX = 0.0
    private var weightY = 0.0
    private var fill = BOTH

    fun xPos(value: Int) = apply { gridX = value }
    fun yPos(value: Int) = apply { gridY = value }
    fun position(gridX: Int, gridY: Int) = apply { this.gridX = gridX; this.gridY = gridY }

    fun xWeight(value: Double) = apply { weightX = value }
    fun yWeight(value: Double) = apply { weightY = value }
    fun weight(weightX: Double, weightY: Double) = apply { this.weightX = weightX; this.weightY = weightY }

    fun xWide(value: Int) = apply { wideX = value }
    fun yWide(value: Int) = apply { wideY = value }
    fun wideness(wideX: Int, wideY: Int) = apply { this.wideX = wideX; this.wideY = wideY }

    fun north(value: Int) = apply { north = value }
    fun east(value: Int) = apply { east = value }
    fun south(value: Int) = apply { south = value }
    fun west(value: Int) = apply { west = value }
    fun padding(north: Int, east: Int, south: Int, west: Int): GridBagPanelAdder {
        this.north = north
        this.west = west
        this.south = south
        this.east = east
        return this
    }

    fun fill(value: Int) = apply { fill = value }

    fun <T: Component>addTo(panel: Container, item: T): T {
        val constraints = GridBagConstraints().apply { anchor = NORTHEAST }
            .apply { fill = this@GridBagPanelAdder.fill }
            .apply { weightx = weightX; weighty = weightY }
            .apply { gridx = gridX; gridy = gridY }
            .apply { gridwidth = wideX; gridheight = wideY }
            .also { it.insets = Insets(north, west, south, east) }
        panel.add(item, constraints)
        return item
    }
}