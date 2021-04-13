package view.utilities.swing

import javax.swing.JTabbedPane

object Tab {

    fun tabs(init: JTabbedPane.() -> Unit) = JTabbedPane().apply(init)
}