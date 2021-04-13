package utility

object ExceptionWrapper {

    fun <R> ensure(function: () -> R): R? = try { function() } catch (_: Exception) { null }
}