package view.utilities.swing

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel

object Label {

    class DescriptionLabel: JPanel() {
        private val _topic = label { text = "-" }
        private val _info = label { text = "-" }

        var topic: String
            get() = _topic.text
            set(value) { _topic.text = value }

        var info: String
            get() = _info.text
            set(value) { _info.text = value }

        init {
            add(_topic)
            add(_info)
        }
    }

    class descriptionLabel(title: String, component: Component): JPanel() {
        init {
            add(label { text = title })
            add(component)
        }
    }

    fun label(init: JLabel.() -> Unit) = JLabel().apply(init)

    fun infoLabel(init: DescriptionLabel.() -> Unit) = DescriptionLabel().apply(init)
}