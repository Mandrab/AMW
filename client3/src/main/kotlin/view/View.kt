package view

import common.Request
import common.type.Command
import common.type.Item
import io.reactivex.rxjava3.core.Observer
import common.type.Order
import io.reactivex.rxjava3.core.Observable

/**
 * Interface to an application view.
 * Specifies methods/properties used for updates.
 *
 * @author Paolo Baldini
 */
interface View {

	/**
	 * Can be used to be informed of user inputs
	 */
	val userInput: Observable<Pair<Request, Any>>

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