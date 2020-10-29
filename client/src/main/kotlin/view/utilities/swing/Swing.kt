package view.utilities.swing

import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

object Swing {

    fun frame(init: JFrame.() -> Unit) = JFrame().apply(init)

    fun panel(init: JPanel.() -> Unit) = JPanel().apply(init)

    fun button(init: JButton.() -> Unit) = JButton().apply(init)
}