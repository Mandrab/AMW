package view.user

import controller.Controller.User
import view.utilities.swing.Tab.tabs
import view.utilities.swing.Swing.frame
import java.awt.BorderLayout
import javax.swing.WindowConstants.EXIT_ON_CLOSE

object View {

    operator fun invoke(controller: User) = frame {

        layout = BorderLayout()

        add(tabs {
            add("Shop", Shop({ controller.shopItems() }, { controller.placeOrder(it) }))
            add("History", History { controller.orders().get() })   // TODO make smarter
            addChangeListener {
                when (selectedComponent) {
                    is Shop -> (selectedComponent as Shop).refresh()
                    is History -> (selectedComponent as History).refresh()
                    else -> Unit
                }
            }
        })

        defaultCloseOperation = EXIT_ON_CLOSE

        pack()
        show()
    }
}