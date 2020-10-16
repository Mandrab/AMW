package view.ui

import view.panel.CommandPanel
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import common.type.Command
import common.type.Item
import io.reactivex.rxjava3.functions.Consumer
import view.panel.AddItem
import view.panel.ScriptPanel

/**
 * An implementations of @see AdminPanel interface.
 * That's a macro-panel that contains all necessary sub-panels used by an admin.
 * Defined sub-panel includes a panel to: see warehouse items; see and use
 * available commands; write down a custom script.
 *
 * @author Paolo Baldini
 */
class AdminPanelImpl: JPanel(), AdminPanel {
    /** {@inheritDoc} */
    override val itemsConsumer: Consumer<Collection<Item>> = AddItem()
    /** {@inheritDoc} */
    override val commandsConsumer: Consumer<Collection<Command>> = CommandPanel()

    private val scriptPanel: ScriptPanel = ScriptPanel()

    init {
        layout = BorderLayout()
        val tabbedPane = JTabbedPane()
        tabbedPane.tabPlacement = SwingConstants.LEFT
        tabbedPane.add("Items", itemsConsumer as AddItem)
        tabbedPane.add("Commands", commandsConsumer as CommandPanel)
        tabbedPane.add("Script", scriptPanel)
        add(tabbedPane, BorderLayout.CENTER)
    }
}