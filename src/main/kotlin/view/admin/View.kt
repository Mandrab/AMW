package view.admin

import controller.Controller.Admin
import view.utilities.LoadingPanel
import view.utilities.swing.GlassLayer
import view.utilities.swing.Swing
import view.utilities.swing.Tab.tabs
import view.utilities.swing.Swing.frame
import java.awt.BorderLayout
import java.util.concurrent.TimeUnit

object View {

    operator fun invoke(controller: Admin) = frame {
        contentPane = LoadingPanel { startLoading, stopLoading -> GlassLayer.layer {
            val tabs = tabs {
                //add("Command", Command())
                add("Warehouse", Warehouse(5,5, {
                        controller.addItem(it)
                    }, {
                        controller.removeItem(it)
                    })
                )    // TODO 1 magic numbers; 2 manage error from agent
                //add("Script", Script())
            }
            add(tabs, BorderLayout.CENTER)

            fun updateTabs() = tabs.selectedComponent?.let { component ->
                LoadingPanel.loading(startLoading, stopLoading) {
                    kotlin.runCatching {
                        when (component) {
                            is Warehouse -> component
                                    .refresh(controller.warehouseState().get(2500, TimeUnit.MILLISECONDS))
                            is Command -> component.refresh()
                            is Script -> component.refresh()
                        }
                    }
                }
            }
            updateTabs()

            add(Swing.button {
                text = "refresh"
                addActionListener { updateTabs() }
            }, BorderLayout.NORTH)
        } }
        setBounds(0, 0, 700, 700)   // TODO
    }
}