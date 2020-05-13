package view.ui

import common.type.Item
import io.reactivex.rxjava3.functions.Consumer
import model.Order

/**
 * Interface that specifies what client-panel 'consumes'.
 * Is useful so the view can specify to any ClientPanel to
 * consume input from a publisher/observable.
 *
 * @author Paolo Baldini
 */
interface ClientPanel {

	/**
	 * Admin panel could observe on items info changes
	 */
	val itemsConsumer: Consumer<Collection<Item>>

	/**
	 * Admin panel could observe on orders info changes
	 */
	val ordersConsumer: Consumer<Collection<Order>>
}