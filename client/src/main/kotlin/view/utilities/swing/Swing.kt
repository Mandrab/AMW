package view.utilities.swing

import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants.EXIT_ON_CLOSE

object Swing {

    fun frame(init: JFrame.() -> Unit) = JFrame()
        .apply { defaultCloseOperation = EXIT_ON_CLOSE; pack(); isVisible = true }.apply(init)

    fun panel(init: JPanel.() -> Unit) = JPanel().apply { layout = BorderLayout() }.apply(init)

    fun button(init: JButton.() -> Unit) = JButton().apply(init)
}