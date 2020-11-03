package view.user

import controller.Controller.User
import view.utilities.LoadingPanel
import view.utilities.LoadingPanel.loading
import view.utilities.swing.GlassLayer.layer
import view.utilities.swing.Swing.button
import view.utilities.swing.Swing.frame
import view.utilities.swing.Tab.tabs
import java.awt.BorderLayout.*
import java.util.concurrent.TimeUnit.MILLISECONDS

object View {

    operator fun invoke(controller: User) = frame {
        contentPane = LoadingPanel { startLoading, stopLoading -> layer {
            val tabs = tabs {
                add("Shop", Shop { controller.placeOrder(it) })
                add("History", History())   // TODO make smarter
            }
            add(tabs, CENTER)

            fun updateTabs() = tabs.selectedComponent?.let { component ->
                loading(startLoading, stopLoading) {
                    kotlin.runCatching {
                        when (component) {
                            is Shop -> component.refresh(controller.shopItems().get(2500, MILLISECONDS))
                            is History -> component.refresh(controller.orders().get(2500, MILLISECONDS))
                            else -> Unit
                        }
                    }
                }
            }
            updateTabs()

            add(button {
                text = "refresh"
                addActionListener { updateTabs() }
            }, NORTH)
        } }
        setBounds(0, 0, 700, 700)   // TODO
    }
}