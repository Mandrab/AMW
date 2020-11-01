package view.utilities

import view.utilities.swing.GlassLayer.layer
import view.utilities.swing.GlassLayer.multilayer
import view.utilities.swing.Label.label
import java.awt.Color
import javax.swing.JLayeredPane
import javax.swing.JPanel
import javax.swing.SwingConstants.CENTER

object LoadingPanel {

    operator fun invoke() = layer {
        setBounds(0, 0, 500, 500)

        add(label {
            text = "loading..."
            horizontalAlignment = CENTER
        })

        background = Color(background.red, background.green, background.blue, 220)
        isVisible = true
    }

    operator fun invoke(mainPanel: (startLoading: () -> Unit, stopLoading: () -> Unit) -> JPanel) = multilayer {
        val loadingLayer = LoadingPanel()
        add(loadingLayer, JLayeredPane.DEFAULT_LAYER, 0)

        val panel = mainPanel({ moveToFront(loadingLayer) }, { moveToBack(loadingLayer) })
        add(panel, JLayeredPane.DEFAULT_LAYER, 0)

        loadingLayer.setBounds(0, 0, 500, 500)  // TODO
        panel.setBounds(0, 0, 500, 500) // TODO
    }
}