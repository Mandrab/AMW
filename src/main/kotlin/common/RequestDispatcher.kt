package common

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer

/**
 * Interface for dispatch request from the whole program
 *
 * @author Paolo Baldini
 */
interface RequestDispatcher {

    /**
     * Try to register a request-consumer.
     *
     * @return a disposable to cancel registration
     */
    fun register(s: Consumer<Pair<Request, Array<out Any>>>): Disposable

    /**
     * Dispatch request
     */
    fun dispatch(request: Request)

    /**
     * Dispatch request and passed params
     */
    fun dispatch(request: Request, vararg args: Any)
}