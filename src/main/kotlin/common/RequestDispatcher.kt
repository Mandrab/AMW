package common

/**
 * Interface for dispatch request from the whole program
 *
 * @author Paolo Baldini
 */
interface RequestDispatcher {

    /**
     * Try to register and handler.
     * Return:
     *      false if handler is already registered or if operation fails,
     *      true otherwise
     */
    fun register(handler: RequestHandler): Boolean

    /**
     * Try to unregister and handler.
     * Return:
     *      false if handler is not registered or if operation fails,
     *      true otherwise
     */
    fun unregister(handler: RequestHandler): Boolean

    /**
     * Dispatch request and passed params
     */
    fun dispatch(request: Request, vararg args: String)
}