package view.panel

import asl.action.load_commands
import common.RequestDispatcherImpl
import common.Request.EXEC_SCRIPT
import view.util.GridBagPanelAdder
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * TODO this class is a residual of an older test. I will conserve it for the moment
 *
 * @author Paolo Baldini
 */
class ScriptPanel: JPanel() {
    private fun requirementsAndScript(s: String): Array<String> {
        val list: MutableList<String> = load_commands.getRequirements(s) as MutableList<String>
        list.add(0, load_commands.getScript(s))
        return list.toTypedArray()
    }

    init {
        layout = GridBagLayout()
        val script = JTextArea() // script
        GridBagPanelAdder().position(0, 0).wideness(5, 5).weight(1.0, 0.9).padding(0, 0, 10, 0)
            .addTo(this, script)
        val execButton = JButton("Run!") // submit button
        GridBagPanelAdder().position(4, 5).fill(GridBagConstraints.VERTICAL)
            .addTo(this, execButton)
        execButton.addActionListener {
            try {
                RequestDispatcherImpl.dispatch(EXEC_SCRIPT, *requirementsAndScript(script.text))
            } catch (ex: Exception) {
                ex.printStackTrace()
                JOptionPane.showMessageDialog(
                    this, "Your input isn't compatible with the application...",
                    "Input error!", JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
}