package common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Test request-dispatcher
 *
 * @author Paolo Baldini
 */
class TestRequestDispatcher {

	@Test fun testSingleton() {
		assert(RequestDispatcherImpl.get === RequestDispatcherImpl.get)
	}

	@Test fun testRegistrations() {
		val dispatcher = RequestDispatcherImpl.get
		val handler = object: RequestHandler {
			override fun askFor(request: Request, vararg args: String) { }
		}
		assert(dispatcher.register(handler))
		assertFalse(dispatcher.register(handler))

		assert(dispatcher.register(object: RequestHandler {
			override fun askFor(request: Request, vararg args: String) { }
		}))

		assert(dispatcher.unregister(handler))
		assertFalse(dispatcher.unregister(handler))
	}

	@Test fun testDispatch() {
		val dispatcher = RequestDispatcherImpl.get
		var dispatched = 0

		assert(dispatcher.register(object: RequestHandler {
			override fun askFor(request: Request, vararg args: String) {
				if (request == Request.ADD_COMMAND) dispatched += 2
				else dispatched++
			}
		}))
		assert(dispatcher.register(object: RequestHandler {
			override fun askFor(request: Request, vararg args: String) { dispatched++ }
		}))

		dispatcher.dispatch(Request.ADD_COMMAND)
		assertEquals(3, dispatched)
		dispatcher.dispatch(Request.EXEC_COMMAND)
		assertEquals(5, dispatched)
	}
}