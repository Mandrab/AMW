import controller.Controller
import controller.SystemRoles

/**
 * Launch application through start of controller.
 *
 * @author Paolo Baldini
 */
fun main(args: Array<String>) {
    Controller(
        role = if (args.any { it == "admin" }) SystemRoles.ADMIN else SystemRoles.USER,
        retryConnection = args.any { it == "retry" }
    )
}