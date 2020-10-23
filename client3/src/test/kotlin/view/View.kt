package view

import common.type.Command
import common.type.Command.Version
import common.type.Item
import common.type.User
import io.reactivex.rxjava3.core.Observable
import common.type.Order
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Bootable class with manual test purpose only (at least at the moment)
 *
 * @author Paolo Baldini
 */
fun main() {
	val view: View = ViewImpl(User.DEBUG)

	listOf(
		Item("id1", 3, arrayListOf(Triple(0, 0, 2), Triple(0, 1, 3))),
		Item("id2", 2, arrayListOf(Triple(1, 0, 2), Triple(1, 1, 3))),
		Item("id3", 5, arrayListOf(Triple(2, 0, 2), Triple(2, 1, 3)))
	).let { view.itemObserver.onNext(it) }

	Executors.newSingleThreadScheduledExecutor().schedule({
		listOf(
			Item("id1", 4, arrayListOf(Triple(0, 0, 2), Triple(0, 1, 3))),
			Item("id2", 2, arrayListOf(Triple(1, 0, 2), Triple(1, 1, 3))),
			Item("id3", 5, arrayListOf(Triple(2, 0, 2), Triple(2, 1, 3)))
		).let { view.itemObserver.onNext(it) }
	}, 5, TimeUnit.SECONDS)

	val orders = listOf(
		Order(
			"id1",
			Order.Status.SUBMITTED,
			listOf(Pair("id1", 1), Pair("id2", 2))
		),
		Order(
			"id2",
			Order.Status.SUBMITTED,
			listOf(Pair("id1", 2), Pair("id3", 1))
		)
	)
	Observable.fromArray<Collection<Order>>(orders).subscribe(view.orderObserver)

	val commands = listOf(
		Command("id1", "name1", "description 1", listOf(
			Version("id_v 1", listOf("req_v 1,1", "req_v 1,2", "req_v 1,3"), "script_v 1"),
			Version("id_v 2", listOf("req_v 2,1", "req_v 2,2", "req_v 2,3"), "script_v 2")
		)),
		Command("id2", "name2", "description 2", listOf(Version("id_v 1", listOf("req_v 1,1", "req_v 1,2", "req_v 1,3"), "script_v 1")))
	)
	Observable.fromArray<Collection<Command>>(commands).subscribe(view.commandObserver)
}