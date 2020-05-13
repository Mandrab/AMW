package controller

import common.type.User
import controller.agent.AgentUtils
import controller.agent.admin.AdminAgent
import controller.agent.admin.AdminProxy
import controller.agent.client.ClientAgent
import controller.agent.client.ClientProxy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import model.ModelImpl
import view.ViewImpl

/**
 * Main class of the application that create agents and main data flow
 *
 * @param user specify user type. (use for debug purpose only)
 * @param retryConnection run application keeping retry connection if it fails
 *
 * @author Paolo Baldini
 */
class ControllerImpl(user: User = User.DEBUG, retryConnection: Boolean = true) {

	init {
		val model = ModelImpl()
		val view = ViewImpl(user)

		// start client agent
		if (user == User.CLIENT || user == User.DEBUG) {
			val clientProxy = ClientProxy()
			AgentUtils.startAgent(ClientAgent::class.java, clientProxy, retryConnection)

			clientProxy.subscribeItems(inlineOnNextObserver { model.items = it.toSet() })
			clientProxy.subscribeOrder(inlineOnNextObserver { model.addOrder(it) })
			clientProxy.subscribeOrder(inlineOnNextObserver { view.orderObserver.onNext(model.orders) })
			clientProxy.subscribeItems(view.itemObserver)
		}

		// start admin agent
		if (user == User.ADMIN || user == User.DEBUG) {
			val adminProxy = AdminProxy()
			AgentUtils.startAgent(AdminAgent::class.java, adminProxy, retryConnection)

			adminProxy.subscribeCommands(inlineOnNextObserver { model.commands = it.toSet() })
			adminProxy.subscribeCommands(view.commandObserver)
		}
	}

	private fun <T>inlineOnNextObserver(action: (param: T) -> Unit) = object: Observer<T> {
		override fun onNext(t: T) { action(t) }
		override fun onComplete() { }
		override fun onSubscribe(d: Disposable) { }
		override fun onError(e: Throwable) { }
	}
}