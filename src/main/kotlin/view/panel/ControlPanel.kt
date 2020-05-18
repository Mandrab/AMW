package view.panel

import view.util.GridBagPanelAdder
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.plaf.basic.BasicBorders
import common.type.Item
import io.reactivex.rxjava3.functions.Consumer
import kotlin.math.floor

/**
 * A panel structured for allow to see items and
 * agent behaviour (through log) in warehouse
 *
 * @author Paolo Baldini
 */
class ControlPanel(private val rackNumber: Int): JPanel(), Consumer<Collection<Item>> {

    private val graphicPanel: JPanel
    private val textualArea: JTextArea

    override fun accept(items: Collection<Item>) {
        (0..rackNumber).forEach { i: Int ->
            if (graphicPanel.getComponent(i) is JPanel) {
                val rackPanel = graphicPanel.getComponent(i) as JPanel
                rackPanel.removeAll()
                val title = JTextArea("Rack number: $i")
                title.background = Color(160, 175, 250)
                rackPanel.add(title)
            }
        }

        items.onEach { item ->
            item.positions.onEach {
                val rackPanel = graphicPanel.getComponent(it.first) as JPanel
                val shelfN = JTextArea("Shelf number: " + it.second)
                shelfN.background = Color(160, 195, 250)
                rackPanel.add(shelfN)
                val itemInfo = JTextArea(item.itemId + ", " + it.third)
                itemInfo.background = Color(160, 210, 250)
                rackPanel.add(itemInfo)
            }
        }
        textualArea.text = items.joinToString { i: Item -> "$i".trimIndent() }
    }

    init {
        layout = GridBagLayout()
        isVisible = true
        graphicPanel = JPanel(GridLayout(floor(rackNumber / 2.0).toInt(), floor(rackNumber / 2.0).toInt()))
        GridBagPanelAdder().position(0, 0).weight(1.0, 0.6)
            .addTo(this, graphicPanel)
        textualArea = JTextArea()
        GridBagPanelAdder().position(0, 1).weight(1.0, 0.4)
            .addTo(this, textualArea)
        (0..10).map { JPanel() }.forEach { rack: JPanel ->
                rack.border = BasicBorders.getTextFieldBorder()
                rack.layout = BoxLayout(rack, BoxLayout.Y_AXIS)
                graphicPanel.add(rack)
            }
    }
}