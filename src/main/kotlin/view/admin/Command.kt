package view.admin

import common.ontology.dsl.abstraction.Command.Command
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Label.infoLabel
import view.utilities.swing.List.List
import view.utilities.swing.List.list
import view.utilities.swing.List.render
import view.utilities.swing.Swing.button
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class Command(
    private val commandsSupplier: () -> Collection<Command>,
    private val runCommand: (command: Command) -> Unit
): JPanel() {
    private val commands: List<Command>

    init {
        layout = GridBagLayout()

        val id = infoLabel { topic = "ID" }
        add(id, constraint { gridx = 1; gridy = 0 })

        val name = infoLabel { topic = "Name" }
        add(name, constraint { gridx = 1; gridy = 1 })

        val description = infoLabel { topic = "Description" }
        add(description, constraint { gridx = 1; gridy = 2; gridwidth = 2 })

        val script = infoLabel {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            topic = "script"
        }
        add(script, constraint { gridx = 1; gridy = 3; gridheight = 5 })

        commands = list {
            elements = commandsSupplier()
            cellRenderer = render { " ID: ${it.id.name}    Name: ${it.name.name} " }
            onClick = {
                id.info = it.id.name
                name.info = it.name.name
                description.info = it.description.name
                script.info = it.script.script.replace(";", ";\n")
            }
        }
        add(commands, constraint { gridx = 0; gridy = 0; gridheight = 10 })

        add(button {
            text = "Execute"
            addActionListener { runCommand(commands.selectedValue) }
        }, constraint { gridx = 1; gridy = 9 })
    }

    fun refresh() { commands.elements = commandsSupplier() }
}
