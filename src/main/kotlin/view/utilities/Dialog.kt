package view.utilities

import javax.swing.JOptionPane

object Dialog {

    object Info {

        operator fun invoke(message: String, title: String = "info") {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
        }
    }
}