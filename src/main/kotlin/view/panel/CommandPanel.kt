package view.panel

import common.Request.ADD_VERSION
import common.Request.EXEC_COMMAND
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
    private val commandsList = ComponentsBuilder.createList(Vector(commands.map { it.id }),
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
                execButton.isEnabled = false
                selectedElem = commands.elementAt(commandsList.selectedIndex)
                commandName.text = "ID: " + selectedElem?.name
                commandDescription.text = "Description: " + selectedElem?.description
                versionsList.setListData(selectedElem?.versions?.map { obj: Command.Version -> obj.id }?.toTypedArray())
                requirementsList.setListData(Vector())
                commandScript.text = "[Script]"
                newVersionButton.isEnabled = true
            }
        })
        versionsList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (!versionsList.isSelectionEmpty) {
                    execButton.isEnabled = true
                    commandScript.text = selectedElem?.versions?.get(versionsList.selectedIndex)?.script
                        ?.replace("}, {", ".\n\n")?.replace(";", ";\n\t")
                        ?.removeSurrounding("\"[  {", "}]\"") + "."
                    requirementsList.setListData(selectedElem?.versions?.get(versionsList.selectedIndex)?.requirements
                        ?.toTypedArray())
                }
            }
        })
        newCommandButton.addActionListener {
            val panel = NewCommandPanel()
            if (JOptionPane.showConfirmDialog(null, panel, "New Command", JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                ViewImpl.publish(ADD_VERSION, panel.idArea, panel.requirementsArea, panel.scriptArea)
            }
        }
        newVersionButton.addActionListener {
            val panel = NewVersionPanel()
            if (JOptionPane.showConfirmDialog(null, panel, "New Version", JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                ViewImpl.publish(ADD_VERSION, panel.idArea, panel.requirementsArea, panel.scriptArea)
            }
        }
        execButton.addActionListener {// TODO
            ViewImpl.publish(EXEC_COMMAND, selectedElem?.id as Any, versionsList.selectedValue)
        }
    }

    override fun accept(t: Collection<Command>) {
        if (commands.addAll(t.groupBy { it.id }.map { Command(it.key, it.value[0].name, it.value[0].description, it.value
            .map { c -> c.versions }.reduce { l1, l2 -> l1.toMutableSet().apply { addAll(l2) }.toList() }) }))
            commandsList.setListData(Vector(commands.map { it.id }))
    }

    private open class NewVersionPanel: JPanel() {
        val idArea = JTextField("Version ID")
        val requirementsArea = JTextField("Requirements (e.g. req1, req2, req3, ...)")
        val scriptArea = JTextArea("Script").also { it.lineWrap = true }

        /**
         * Creates the GUI shown inside the frame's content pane.
         * */
        init {
            layout = GridBagLayout()
            GridBagPanelAdder().weight(1.0, .0).addTo(this, idArea)
            GridBagPanelAdder().yPos(1).weight(1.0, .0).addTo(this, requirementsArea)
            GridBagPanelAdder().yPos(2).weight(1.0, 1.0).addTo(this, scriptArea)

            preferredSize = Dimension(400, 400)
        }
    }

    private class NewCommandPanel: NewVersionPanel() {
        val commandIdArea = JTextField("Command ID")
        val nameArea = JTextField("Command name")
        val descriptionArea = JTextArea("Description").also { it.lineWrap = true }

        /**
         * Creates the GUI shown inside the frame's content pane.
         * */
        init {
            layout = GridBagLayout()
            GridBagPanelAdder().weight(1.0, .0).addTo(this, commandIdArea)
            GridBagPanelAdder().yPos(1).weight(1.0, .0).addTo(this, nameArea)
            GridBagPanelAdder().yPos(2).weight(1.0, 1.0).addTo(this, JScrollPane(descriptionArea))
            GridBagPanelAdder().position(1, 0).weight(1.0, .0).addTo(this, super.idArea)
            GridBagPanelAdder().position(1, 1).weight(1.0, .0).addTo(this, super.requirementsArea)
            GridBagPanelAdder().position(1, 2).weight(1.0, 1.0).addTo(this, JScrollPane(super.scriptArea))

            preferredSize = Dimension(400, 400)
        }
    }
}