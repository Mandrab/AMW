package common

import io.reactivex.rxjava3.functions.Consumer
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test request-dispatcher
 *
 * @author Paolo Baldini
 */
class TestRequestDispatcher {

	@Test fun testRequestDispatch() {
		val dispatcher = RequestDispatcherImpl
		var dispatched = 0

		dispatcher.register(Consumer<Pair<Request, Array<out Any>>> {
				if (it.first == Request.ADD_COMMAND) dispatched += 2
				else dispatched++
		})
		dispatcher.register(Consumer<Pair<Request, Array<out Any>>> { dispatched++ })

		dispatcher.dispatch(Request.ADD_COMMAND)
		assertEquals(3, dispatched)
		dispatcher.dispatch(Request.EXEC_COMMAND)
		assertEquals(5, dispatched)
	}

	@Test fun testArgsDispatch() {
		val dispatcher = RequestDispatcherImpl
		var dispatched = 0

		dispatcher.register(Consumer<Pair<Request, Array<out Any>>> {
			if (it.second.isNotEmpty() && it.second[0] is Int) {
				dispatched += 2
			} else dispatched++
		})

		dispatcher.dispatch(Request.ADD_COMMAND)
		assertEquals(1, dispatched)
		dispatcher.dispatch(Request.ADD_COMMAND, String())
		assertEquals(2, dispatched)
		dispatcher.dispatch(Request.EXEC_COMMAND, 5)
		assertEquals(4, dispatched)
	}
}