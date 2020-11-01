package view.user

import controller.Controller.User
import view.utilities.LoadingPanel
import view.utilities.swing.GlassLayer.layer
import view.utilities.swing.Swing.button
import view.utilities.swing.Swing.frame
import view.utilities.swing.Tab.tabs
import java.awt.BorderLayout.*
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

object View {

    operator fun invoke(controller: User) = frame {
        contentPane = LoadingPanel { startLoading, stopLoading ->
            layer {
                val tabs = tabs {
                    add("Shop", Shop { controller.placeOrder(it) })
                    add("History", History())   // TODO make smarter
                }
                add(tabs, CENTER)

                add(button {
                    text = "refresh"
                    addActionListener {
                        val component = tabs.selectedComponent
                        loading(startLoading, stopLoading) {
                            kotlin.runCatching {
                                when (component) {
                                    is Shop -> component
                                        .refresh(controller.shopItems().get(2500, TimeUnit.MILLISECONDS))
                                    is History -> component.refresh(emptyList())//.refresh(controller.orders().get()) TODO
                                    else -> Unit
                                }
                            }
                        }
                    }
                }, NORTH)
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