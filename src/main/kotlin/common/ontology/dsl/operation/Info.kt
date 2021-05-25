package common.ontology.dsl.operation

/**
 * Represents 'info' operation(s) in the system
 * It refer to ontology abstractions
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Info {

    /**
     * Represents possible info request target
     *
     * @author Paolo Baldini
     */
    enum class Target {
        WAREHOUSE,
        COMMANDS
    }

    data class Info(val target: Target)

    fun info(target: Target) = Info(target)
}
