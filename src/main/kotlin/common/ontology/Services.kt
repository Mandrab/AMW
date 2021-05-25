package common.ontology

/**
 * Represents possible services offered by the agents of the system
 *
 * @author Paolo Baldini
 */
object Services {

    /**
     * Possible services supplier in the system
     *
     * @author Paolo Baldini
     */
    enum class ServiceSupplier(val id: String) {
        EXECUTOR_COMMAND("executor(command)"),
        MANAGEMENT_COMMANDS("management(commands)"),
        MANAGEMENT_ITEMS("management(items)"),
        MANAGEMENT_ORDERS("management(orders)"),
        PICKER_ITEMS("executor(item_picker)")
    }

    /**
     * Possible services type in the system
     *
     * @author Paolo Baldini
     */
    enum class ServiceType(val id: String) {
        ACCEPT_ORDER("accept(order)"),
        ADD_COMMAND("add(command)"),
        EXEC_COMMAND("exec(command)"),
        INFO_COMMANDS("info(commands)"),
        INFO_COLLECTION_POINTS("info(collection_points)"),
        INFO_ORDERS("info(orders)"),
        INFO_WAREHOUSE("info(warehouse)"),
        REMOVE_ITEM("remove(item)"),
        RETRIEVE_ITEM("retrieve(item)"),
        STORE_ITEM("store(item)");
    }
}
