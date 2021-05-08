package common.ontology

object Services {

    /**
     * Possible services supplier in the system
     *
     * @author Paolo Baldini
     */
    enum class ServiceSupplier(val id: String) {
        MANAGEMENT_COMMANDS("management(commands)"),
        MANAGEMENT_ITEMS("management(items)"),
        MANAGEMENT_ORDERS("management(orders)"),
        EXECUTOR_COMMAND("executor(command)"),
        EXECUTOR_SCRIPT("executor(command)"),
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
        ADD_VERSION("add(version)"),
        INFO_COMMANDS("info(commands)"),
        INFO_COLLECTION_POINTS("info(collection_points)"),
        INFO_ORDERS("info(orders)"),
        INFO_WAREHOUSE("info(warehouse)"),
        EXEC_COMMAND("exec(command)"),
        EXEC_SCRIPT("exec(command)"),
        REMOVE_ITEM("remove(item)"),
        RETRIEVE_ITEM("retrieve(item)"),
        STORE_ITEM("store(item)");
    }
}