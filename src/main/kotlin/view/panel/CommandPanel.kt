package view.panel

import view.util.ComponentsBuilder
import view.util.GridBagPanelAdder
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextArea
import common.type.Command
import common.RequestDispatcherImpl
import common.Request.EXEC_COMMAND
import io.reactivex.rxjava3.functions.Consumer

/**
 * A panel structured for allow an administrator to request the execution
 * of a command saved in a remote repository.
 *
 * @author Paolo Baldini
 */
class CommandPanel : JPanel(), Consumer<Collection<Command>> {
    private var commands = setOf<Command>()
    private var commandsList = ComponentsBuilder.createList(Vector(commands.map { it.id }),
        commands.size.coerceAtMost(25), 60)
    private var selectedElem: Command? = null

    init {
        layout = GridBagLayout()

        val commandName = JTextArea("Name: -").also { it.isEditable = false } // command name
        val commandDescription = JTextArea("Description: -").also { it.isEditable = false } // command description
        val commandScript = JTextArea("[Script]").also { it.isEditable = false } // version script
        val versionsList: JList<String> = ComponentsBuilder.createList(Vector(), 25, 60) // command's versions list
        val requirementsList: JList<String> = ComponentsBuilder.createList(Vector(), 25, 60) // version's requirements list
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
        execButton.addActionListener {// TODO
            RequestDispatcherImpl.dispatch(EXEC_COMMAND, selectedElem?.id as Any, versionsList.selectedValue)
        }
    }

    override fun accept(t: Collection<Command>) {
        commands = t.groupBy { it.id }.map { Command(it.key, it.value[0].name, it.value[0].description, it.value
            .map { c -> c.versions }.reduce { l1, l2 -> l1.toMutableSet().apply { addAll(l2) }.toList() }) }.toSet()
        commandsList.setListData(Vector(commands.map { it.id }))
    }
}