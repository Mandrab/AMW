package common.translation

/**
 * Possible services type in the system
 *
 * @author Paolo Baldini
 */
enum class Service(val service: String) {
    MANAGEMENT_ORDERS("management( orders )"),
    ACCEPT_ORDER("accept( order )"),
    MANAGEMENT_ITEMS("management( items )"),
    INFO_WAREHOUSE("info( warehouse )"),
    MANAGEMENT_COMMANDS("management( commands )"),
    INFO_COMMANDS("info( commands )"),
    EXECUTOR_COMMAND("executor( command )"),
    EXEC_COMMAND("exec( command )"),
    EXECUTOR_SCRIPT("executor( command )"),
    EXEC_SCRIPT("exec( command )");
}