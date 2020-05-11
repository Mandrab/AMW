package common

/**
 * Implementation of request dispatcher. See @see RequestDispatcher
 *
 * @author Paolo Baldini
 */
class RequestDispatcherImpl private constructor(): RequestDispatcher {
    companion object {
        val get: RequestDispatcher = RequestDispatcherImpl()
    }

    private val handlers: MutableSet<RequestHandler> = mutableSetOf()

    /**
     * {@inheritDoc}
     */
    override fun register(handler: RequestHandler) = handlers.add(handler)

    /**
     * {@inheritDoc}
     */
    override fun unregister(handler: RequestHandler) = handlers.remove(handler)

    /**
     * {@inheritDoc}
     */
    override fun dispatch(request: Request, vararg args: String) = handlers.forEach { it.askFor(request, *args) }
}