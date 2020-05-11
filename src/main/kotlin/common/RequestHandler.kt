package common

/**
 * Interface for commands acceptance
 *
 * @author Paolo Baldini
 */
interface RequestHandler {

    /**
     * Accept a request and consider eventually passed params
     */
    fun askFor(request: Request, vararg args: String)
}