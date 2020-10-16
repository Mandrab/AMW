package util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ResultLock<T: Any>(alternativeResult: T): ReentrantLock() {
	private var value: Boolean = false
	private val lock = newCondition()
	var result: T = alternativeResult

	fun maxTimeToComplete(maxMillis: Int) = withLock {
		if (!value) lock.await(maxMillis.toLong(), TimeUnit.MILLISECONDS)
		value = true
	}

	fun tryComplete(action: () -> T) = withLock {
		if (!value) {
			result = action()
			value = true
			lock.signalAll();
		}
	}
}