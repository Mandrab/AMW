package view.ui

import common.type.Command
import common.type.Item
import io.reactivex.rxjava3.functions.Consumer

/**
 * Interface that specifies what admin-panel 'consumes'.
 * Is useful so the view can specify to any AdminPanel to
 * consume input from a publisher/observable.
 *
 * @author Paolo Baldini
 */
interface AdminPanel {

	/**
	 * Admin panel could observe on items info changes
	 */
	val itemsConsumer: Consumer<Collection<Item>>

	/**
	 * Admin panel could observe on commands info changes
	 */
	val commandsConsumer: Consumer<Collection<Command>>
}