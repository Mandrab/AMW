package common

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Implementation of request dispatcher. See @see RequestDispatcher
 *
 * @author Paolo Baldini
 */
object RequestDispatcherImpl: RequestDispatcher {
    private val dispatcher = PublishSubject.create<Pair<Request, Array<out Any>>>()

    /** {@inheritDoc} */
    override fun register(s: Consumer<Pair<Request, Array<out Any>>>): Disposable = dispatcher.subscribe(s)

    /** {@inheritDoc} */
    override fun dispatch(request: Request) = dispatcher.onNext(Pair(request, emptyArray()))

    /** {@inheritDoc} */
    override fun dispatch(request: Request, vararg args: Any) = dispatcher.onNext(Pair(request, args))
}