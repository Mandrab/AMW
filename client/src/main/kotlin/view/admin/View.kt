package view.admin

import controller.Controller.Admin
import view.utilities.swing.Tab.tabs
import view.utilities.swing.Swing.frame
import java.awt.BorderLayout
import javax.swing.WindowConstants.EXIT_ON_CLOSE

object View {

    operator fun invoke(controller: Admin) = frame {

        layout = BorderLayout()

        add(tabs {
            //add("Warehouse", Warehouse())
            add("Command", Command({ emptyList() }, { }))
            //add("Script", Script())
            addChangeListener {
                when (selectedComponent) {
                    is Warehouse -> (selectedComponent as Warehouse).refresh()
                    is Command -> (selectedComponent as Command).refresh()
                    is Script -> (selectedComponent as Script).refresh()
                    else -> Unit
                }
            }
        })

        defaultCloseOperation = EXIT_ON_CLOSE

        pack()
        show()
    }
}