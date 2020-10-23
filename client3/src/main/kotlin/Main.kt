import common.type.User
import controller.ControllerImpl

/**
 * Launch application through start of controller.
 *
 * TODO: allow other cli interaction to specify more possibilities
 *
 * @author Paolo Baldini
 */
fun main(args: Array<String>) {
    ControllerImpl(User.DEBUG, args.isNotEmpty() && args[0] == "retry", args.copyOfRange(1, args.size))
}