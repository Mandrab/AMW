package view.panel

import common.Request.*
import common.type.Command
import io.reactivex.rxjava3.functions.Consumer
import view.ViewImpl
import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.Dimension
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*


/**
 * A panel structured for allow an administrator to request the execution
 * of a command saved in a remote repository.
 *
 * @author Paolo Baldini
 */
class CommandPanel : JPanel(), Consumer<Collection<Command>> {
    private val commands = mutableSetOf<Command>()
    private val commandsList = ComponentsBuilder.createList(Vector(commands.map { it.id.removeSurrounding("\"") }),
        commands.size.coerceAtMost(25), 60)
    private var selectedElem: Command? = null

    init {
        layout = GridBagLayout()

        val commandName = JTextArea("Name: -").also { it.isEditable = false } // command name
        val commandDescription = JTextArea("Description: -").also { it.isEditable = false } // command description
        val commandScript = JTextArea("[Script]").also { it.isEditable = false } // version script
        val versionsList: JList<String> = ComponentsBuilder.createList(Vector(), 25, 60) // command's versions list
        val requirementsList: JList<String> = ComponentsBuilder.createList(Vector(), 25, 60) // version's requirements list
        val newCommandButton = JButton("New Command")
        val newVersionButton = JButton("New Version").also { isEnabled = false }
        val execButton = JButton("Exec") // submit button

        // COMPONENTS SETUP
        execButton.isEnabled = false

        // TODO list requirements

        // COMPONENTS PLACEMENT
        GridBagPanelAdder().xPos(1).xWide(2).weight(.33, .0).addTo(this, commandName)
        GridBagPanelAdder().position(1, 1).xWide(2).xWeight(.33).addTo(this, commandDescription)
        GridBagPanelAdder().position(1, 3).xWide(2).weight(.9, .77).addTo(this, commandScript)
        GridBagPanelAdder().yWide(4).weight(.1, 1.0).east(10).addTo(this, commandsList)
        GridBagPanelAdder().position(1, 2).weight(.5, .33).padding(5, 0, 5, 0)
            .addTo(this, versionsList)
        GridBagPanelAdder().position(2, 2).weight(.5, .33).padding(5, 0, 5, 0).addTo(this, requirementsList)
        GridBagPanelAdder().position(0, 6).fill(HORIZONTAL).addTo(this, newCommandButton)
        GridBagPanelAdder().position(1, 6).fill(HORIZONTAL).addTo(this, newVersionButton)
        GridBagPanelAdder().position(2, 6).fill(HORIZONTAL).addTo(this, execButton)

        // LISTENERS
        commandsList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                execButton.isEnabled = true
                selectedElem = commands.elementAt(commandsList.selectedIndex)
                commandName.text = "ID: " + selectedElem?.name?.removeSurrounding("\"")
                commandDescription.text = "Description: " + selectedElem?.description?.removeSurrounding("\"")
                versionsList.setListData(selectedElem?.versions?.map { obj: Command.Version -> obj.id }?.map { it.removeSurrounding("\"") }?.toTypedArray())
                requirementsList.setListData(Vector())
                commandScript.text = "[Script]"
                newVersionButton.isEnabled = true
            }
        })
        versionsList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (!versionsList.isSelectionEmpty) {
                    commandScript.text = selectedElem?.versions?.get(versionsList.selectedIndex)?.script?.removeSurrounding("\"")
                        ?.replace("}, {", ".\n\n")?.replace(";", ";\n\t")
                        ?.removeSurrounding("\"[  {", "}]\"") + "."
                    requirementsList.setListData(selectedElem?.versions?.get(versionsList.selectedIndex)?.requirements
                        ?.map { it.removeSurrounding("\"") }?.toTypedArray())
                }
            }
        })
        newCommandButton.addActionListener {
            val panel = NewCommandPanel()
            if (JOptionPane.showConfirmDialog(null, panel, "New Command", JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                ViewImpl.publish(ADD_COMMAND, Command(panel.commandID.text, panel.name.text, panel.description.text, listOf(
                    Command.Version(panel.versionID.text, panel.requirements.text.split(","), panel.script.text)
                )))
            }
        }
        newVersionButton.addActionListener {
            val panel = NewVersionPanel()
            if (JOptionPane.showConfirmDialog(null, panel, "New Version", JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                ViewImpl.publish(ADD_VERSION, arrayOf(selectedElem!!.id, Command.Version(panel.versionID.text, panel.requirements.text.split(","), panel.script.text)))
            }
        }
        execButton.addActionListener { ViewImpl.publish(EXEC_COMMAND, selectedElem!!.id) }
    }

    override fun accept(t: Collection<Command>) {
        if (commands.addAll(t.groupBy { it.id }.map { Command(it.key, it.value[0].name, it.value[0].description, it.value
            .map { c -> c.versions }.reduce { l1, l2 -> l1.toMutableSet().apply { addAll(l2) }.toList() }) }))
            commandsList.setListData(Vector(commands.map { it.id.removeSurrounding("\"") }))
    }

    private open class NewVersionPanel: JPanel() {
        val versionID = JTextField("Version ID")
        val requirements = JTextField("Requirements (e.g. req1, req2, req3, ...)")
        val script = JTextArea("Script").also { it.lineWrap = true }

        /**
         * Creates the GUI shown inside the frame's content pane.
         * */
        init {
            layout = GridBagLayout()
            GridBagPanelAdder().weight(1.0, .0).addTo(this, versionID)
            GridBagPanelAdder().yPos(1).weight(1.0, .0).addTo(this, requirements)
            GridBagPanelAdder().yPos(2).weight(1.0, 1.0).addTo(this, script)

            preferredSize = Dimension(400, 400)
        }
    }

    private class NewCommandPanel: NewVersionPanel() {
        val commandID = JTextField("Command ID")
        val name = JTextField("Command name")
        val description = JTextArea("Description").also { it.lineWrap = true }

        /**
         * Creates the GUI shown inside the frame's content pane.
         * */
        init {
            layout = GridBagLayout()
            GridBagPanelAdder().weight(1.0, .0).addTo(this, commandID)
            GridBagPanelAdder().yPos(1).weight(1.0, .0).addTo(this, name)
            GridBagPanelAdder().yPos(2).weight(1.0, 1.0).addTo(this, JScrollPane(description))
            GridBagPanelAdder().position(1, 0).weight(1.0, .0).addTo(this, super.versionID)
            GridBagPanelAdder().position(1, 1).weight(1.0, .0).addTo(this, super.requirements)
            GridBagPanelAdder().position(1, 2).weight(1.0, 1.0).addTo(this, JScrollPane(super.script))

            preferredSize = Dimension(400, 400)
        }
    }
}