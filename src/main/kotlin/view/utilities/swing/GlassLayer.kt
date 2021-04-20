package view.utilities.swing

import view.utilities.swing.Swing.panel
import javax.swing.JLayeredPane
import javax.swing.JPanel

object GlassLayer {

    fun multilayer(init: JLayeredPane.() ->  Unit) = JLayeredPane().apply(init).apply {
        layout = null
    }

    fun layer(init: JPanel.() -> Unit) = panel(init).apply {
        setBounds(0, 0, preferredSize.width, preferredSize.height)
    }
}