package common.ontology.dsl.abstraction

/**
 * Represents 'order status' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Status {

    /**
     * Possible states of an order in the system
     *
     * @author Paolo Baldini
     */
    enum class States(val value: String) {
        CHECKING("check"),
        COMPLETED("completed"),
        RETRIEVING("retrieve")
    }

    data class Status(val state: States)

    fun status(state: States) = Status(state)
}
