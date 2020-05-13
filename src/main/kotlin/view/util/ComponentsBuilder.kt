package view.util

import java.util.*
import javax.swing.JList
import javax.swing.ListSelectionModel

/**
 * An utility object which contains a method to comfortably create lists
 *
 * @author Paolo Baldini
 */
object ComponentsBuilder {
    fun createList(items: Vector<String>, row: Int, cellW: Int): JList<String> {
        val list = JList(arrayOf(""))
        list.selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION
        list.layoutOrientation = JList.VERTICAL
        list.visibleRowCount = row
        list.fixedCellHeight = if (items.size < 10) list.preferredSize.height * 5 else Math.min(items.size, 25)
        list.fixedCellWidth = cellW
        list.setListData(items)
        return list
    }
}