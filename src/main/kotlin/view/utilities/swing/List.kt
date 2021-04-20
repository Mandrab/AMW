package view.utilities.swing

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

object List {

    class List<T>: JList<T>() {
        private var _elements: Collection<T> = emptyList()
        var elements: Collection<T>
            get() = _elements
            set(value) { _elements = value; setListData(Vector(value)) }

        var onClick: (element: T) -> Unit = { }
            set(value) = addMouseListener(object: MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) = value(elements.elementAt(selectedIndex))
            })
    }

    operator fun <T> List<T>.plusAssign(element: T) = run { elements = elements + element }

    operator fun <T> List<T>.minusAssign(element: T) = run { elements = elements - element }

    fun <T> List<T>.map(condition: (T) -> Boolean, operation: (T) -> T) = run { elements = elements.map {
        if (condition(it)) operation(it) else it
    } }

    fun <T> List<T>.clean() = run { elements = emptyList() }

    fun <T> list(init: List<T>.() -> Unit) = List<T>().apply(init)

    fun <T> render(init: (T) -> String): ListCellRenderer<T> = ListCellRenderer<T> { _, e, _, _, _ -> JLabel(init(e)) }
}