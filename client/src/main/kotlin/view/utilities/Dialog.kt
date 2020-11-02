package view.utilities

import javax.swing.JOptionPane

object Dialog {

    object Info {

        operator fun invoke(message: String) {
            JOptionPane.showMessageDialog(null, message, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}