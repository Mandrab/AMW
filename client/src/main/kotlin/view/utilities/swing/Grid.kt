package view.utilities.swing

import java.awt.GridBagConstraints
import java.awt.Insets

object Grid {

    fun constraint(init: GridBagConstraints.() -> Unit) = GridBagConstraints().apply(init).apply {
        insets = Insets(10, 10, 10, 10)
    }
}