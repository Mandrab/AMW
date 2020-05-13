package view.ui

import common.type.Item
import io.reactivex.rxjava3.functions.Consumer
import model.Order
import view.panel.OrderHistory
import view.panel.NewOrder
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

/**
 * An implementations of @see ClientPanel interface.
 * A macro-panel that contains all necessary sub-panels used by a client.
 * Defined sub-panel includes a panel to: shop some items (beeing a
 * project-application and not a real one, no purchase method is effectively
 * available); see previously submitted orders.
 *
 * @author Paolo Baldini
 */
class ClientPanelImpl: JPanel(), ClientPanel {
	/** {@inheritDoc} */
	override val itemsConsumer: Consumer<Collection<Item>> = NewOrder()
	/** {@inheritDoc} */
	override val ordersConsumer: Consumer<Collection<Order>> = OrderHistory()

	init {
		layout = BorderLayout()
		val tabbedPane = JTabbedPane()
		tabbedPane.tabPlacement = SwingConstants.LEFT
		tabbedPane.add("Shop", itemsConsumer as NewOrder)
		tabbedPane.add("History", ordersConsumer as OrderHistory)
		add(tabbedPane, BorderLayout.CENTER)
	}
}