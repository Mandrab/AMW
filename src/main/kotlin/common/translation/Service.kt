package common.translation

/**
 * Possible services type in the system
 *
 * @author Paolo Baldini
 */
enum class Service(val service: String) {
    MANAGEMENT_ORDERS("management(orders)"),
    MANAGEMENT_ITEMS("management(items)"),
    MANAGEMENT_COMMANDS("management(commands)"),

    ACCEPT_ORDER("accept(order)"),
    INFO_WAREHOUSE("info(warehouse)"),
    INFO_COMMANDS("info(commands)"),
    EXECUTOR_COMMAND("executor(command)"),
    EXEC_COMMAND("exec(command)"),
    EXECUTOR_SCRIPT("executor(command)"),
    EXEC_SCRIPT("exec(command)");
}