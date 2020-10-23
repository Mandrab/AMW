import controller.ControllerImpl
import controller.SystemRoles

/**
 * Launch application through start of controller.
 *
 * TODO: allow other cli interaction to specify more possibilities
 *
 * @author Paolo Baldini
 */
fun main(args: Array<String>) {
    ControllerImpl(
        role = SystemRoles.USER,
        retryConnection = args.isNotEmpty() && args[0] == "retry"
    )
}