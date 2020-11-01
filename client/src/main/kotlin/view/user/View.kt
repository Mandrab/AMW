package view.user

import controller.Controller.User
import view.utilities.LoadingPanel
import view.utilities.swing.GlassLayer.layer
import view.utilities.swing.Swing.frame
import view.utilities.swing.Tab.tabs
import java.util.concurrent.TimeUnit
import javax.swing.JLayeredPane.CENTER_ALIGNMENT
import javax.swing.SwingUtilities

object View {

    operator fun invoke(controller: User) = frame {
        contentPane = LoadingPanel { startLoading, stopLoading ->
            layer {
                add(tabs {
                    add("Shop", Shop { controller.placeOrder(it) })
                    add("History", History())   // TODO make smarter
                    addChangeListener {
                        loading(startLoading, stopLoading) {
                            if(kotlin.runCatching {
                                when (selectedComponent) {
                                    is Shop -> (selectedComponent as Shop)
                                        .refresh(controller.shopItems().get(2500, TimeUnit.MILLISECONDS))
                                    is History -> (selectedComponent as History)
                                        .refresh(emptyList())//.refresh(controller.orders().get()) TODO
                                    else -> Unit
                                }
                            }.isFailure) println("Error! The remote object is requiring to much time to respond!")
                        }
                    }
                }, CENTER_ALIGNMENT)
            }
        }
        setBounds(0, 0, 500, 500)   // TODO
    }

    private fun <T> loading(startLoading: () -> Unit, stopLoading: () -> Unit, operation: () -> T) {
        startLoading()
        SwingUtilities.invokeLater {
            operation()
            stopLoading()
        }
    }
}