package view.utilities.swing

import javax.swing.*

object Swing {

    fun frame(init: JFrame.() -> Unit) = JFrame().apply(init)

    fun button(init: JButton.() -> Unit) = JButton().apply(init)

    fun label(init: JLabel.() -> Unit) = JLabel().apply(init)
}