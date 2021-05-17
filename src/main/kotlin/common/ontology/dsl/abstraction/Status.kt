package common.ontology.dsl.abstraction

object Status {

    enum class States(val value: String) {
        CHECKING("check"),
        COMPLETED("completed"),
        RETRIEVING("retrieve")
    }

    data class Status(val state: States)

    fun status(state: States) = Status(state)
}
