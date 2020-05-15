package view

import common.type.Command
import common.type.Item
import io.reactivex.rxjava3.core.Observer
import common.type.Order

/**
 * Interface to an application view.
 * Specifies methods/properties used for updates.
 *
 * @author Paolo Baldini
 */
interface View {

	/**
	 * View could observe on commands info changes
	 */
	val commandObserver: Observer<Collection<Command>>

	/**
	 * View could observe on items info changes
	 */
	val itemObserver: Observer<Collection<Item>>

	/**
	 * View could observe on order info changes
	 */
	val orderObserver: Observer<Collection<Order>>
}