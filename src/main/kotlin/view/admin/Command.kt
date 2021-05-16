package view.admin

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.Requirement.Requirement
import view.utilities.swing.Grid.constraint
import view.utilities.swing.Label.infoLabel
import view.utilities.swing.Label.label
import view.utilities.swing.List.List
import view.utilities.swing.List.list
import view.utilities.swing.List.render
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
        add(id, constraint { gridx = 1 })

        val name = infoLabel { topic = "Name" }
        add(name, constraint { gridx = 2 })

        val description = infoLabel { topic = "Description" }
        add(description, constraint { gridx = 1; gridy = 1; gridwidth = 2 })

        add(label { text = "Requirements" }, constraint { gridx = 1; gridy = 3 })
        val requirements = list<Requirement> {
            cellRenderer = render { " ID: ${it.name} " }
        }
        add(requirements, constraint { gridx = 1; gridy = 4 })

        val script = infoLabel {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            topic = "script"
        }
        add(script, constraint { gridx = 2; gridy = 2 })

        add(label { text = "Variants" }, constraint { gridx = 1; gridy = 2 })

        commands = list {
            elements = commandsSupplier()
            cellRenderer = render { " ID: ${it.id.name}    Name: ${it.name.name} " }
            onClick = {
                id.info = it.id.name
                name.info = it.name.name
                description.info = it.description.name
            }
        }
    }

    fun refresh() { commands.elements = commandsSupplier() }
}
