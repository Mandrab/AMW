package utility

/**
 * A class that provides a cleaner way to work with exceptions in kotlin
 *
 * @author Paolo Baldini
 */
object ExceptionWrapper {

    fun <R> ensure(function: () -> R): R? = try { function() } catch (_: Exception) { null }
}
