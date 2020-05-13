package view

import common.type.Command
import common.type.Item
import common.RequestDispatcherImpl
import common.Request.END
import common.type.User
import model.Order
import view.panel.ControlPanel
import java.awt.Color
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.subjects.PublishSubject
import view.ui.AdminPanelImpl
import view.ui.ClientPanelImpl
import kotlin.system.exitProcess

/**
 * An implementations of @see View interface. See it for more information
 *
 * @author Paolo Baldini
 */
class ViewImpl(private val user: User): JFrame(), View {
    /** {@inheritDoc} */
    override val commandObserver: Observer<Collection<Command>> = PublishSubject.create()
    /** {@inheritDoc} */
    override val itemObserver: Observer<Collection<Item>> = PublishSubject.create()
    /** {@inheritDoc} */
    override val orderObserver: Observer<Collection<Order>> = PublishSubject.create()

    private val clientPanel: ClientPanelImpl?
    private val adminPanel: AdminPanelImpl?
    private val controlPanel: ControlPanel?

    init {
        if (user == User.CLIENT || user == User.DEBUG) {
            clientPanel = ClientPanelImpl()
            (orderObserver as PublishSubject).subscribe(clientPanel.ordersConsumer)
            (itemObserver as PublishSubject).subscribe(clientPanel.itemsConsumer)
        } else clientPanel = null

        if (user == User.ADMIN || user == User.DEBUG) {
            adminPanel = AdminPanelImpl()
            (commandObserver as PublishSubject).subscribe(adminPanel.commandsConsumer)
            (itemObserver as PublishSubject).subscribe(adminPanel.itemsConsumer)

            controlPanel = ControlPanel(10)
            itemObserver.subscribe(controlPanel)
        } else {
            adminPanel = null
            controlPanel = null
        }

        setupView()
    }

    private fun setupView() {
        title = "Agent Managed Warehouse"

        val tabbedPane = JTabbedPane()

        clientPanel?.let { tabbedPane.add("Client", it) }
        adminPanel?.let { tabbedPane.add("Admin", adminPanel) }
        controlPanel?.let { tabbedPane.add("Warehouse", controlPanel) }

        add(tabbedPane)

        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            // at close ask and stop the agent
            override fun windowClosing(windowEvent: WindowEvent) {
                if (JOptionPane.showConfirmDialog(parent, "Are you sure you want to close this window?",
                        "Close Window?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION ) {
                    RequestDispatcherImpl.dispatch(END)
                    exitProcess(0)
                }
            }
        })

        pack()
        isVisible = true
        minimumSize = Dimension(600, 300)
        setLocationRelativeTo(null)
    }

    private fun setupTheme() {
        UIManager.put("control", Color(128, 128, 128))
        UIManager.put("info", Color(128, 128, 128))
        UIManager.put("nimbusBase", Color(18, 30, 49))
        UIManager.put("nimbusAlertYellow", Color(248, 187, 0))
        UIManager.put("nimbusDisabledText", Color(128, 128, 128))
        UIManager.put("nimbusFocus", Color(115, 164, 209))
        UIManager.put("nimbusGreen", Color(176, 179, 50))
        UIManager.put("nimbusInfoBlue", Color(66, 139, 221))
        UIManager.put("nimbusLightBackground", Color(18, 30, 49))
        UIManager.put("nimbusOrange", Color(191, 98, 4))
        UIManager.put("nimbusRed", Color(169, 46, 34))
        UIManager.put("nimbusSelectedText", Color(255, 255, 255))
        UIManager.put("nimbusSelectionBackground", Color(104, 93, 156))
        UIManager.put("text", Color(230, 230, 230))

        UIManager.getInstalledLookAndFeels().first { it.name == "Nimbus" }.let {
            UIManager.setLookAndFeel(it.className)
        }
    }
}