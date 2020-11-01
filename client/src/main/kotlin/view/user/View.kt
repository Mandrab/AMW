package view.user

import controller.Controller.User
import view.utilities.LoadingPanel
import view.utilities.swing.GlassLayer.layer
import view.utilities.swing.Swing.frame
import view.utilities.swing.Tab.tabs
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
                            when (selectedComponent) {
                                is Shop -> (selectedComponent as Shop).refresh(controller.shopItems().get())
                                is History -> (selectedComponent as History).refresh(emptyList())//.refresh(controller.orders().get())
                                else -> Unit
                            }
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