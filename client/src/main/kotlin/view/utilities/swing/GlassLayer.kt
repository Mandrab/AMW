package view.utilities.swing

import javax.swing.JLayeredPane
import javax.swing.JPanel

object GlassLayer {

    fun multilayer(init: JLayeredPane.() ->  Unit) = JLayeredPane().apply(init).apply {
        layout = null
    }

    fun layer(init: JPanel.() -> Unit) = JPanel().apply(init).apply {
        setBounds(0, 0, preferredSize.width, preferredSize.height)
    }
}