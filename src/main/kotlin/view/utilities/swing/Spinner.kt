package view.utilities.swing

import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

object Spinner {

    fun spinner(default: Int, min: Int, max: Int, step: Int = 1) = JSpinner(SpinnerNumberModel(default, min, max, step))
}