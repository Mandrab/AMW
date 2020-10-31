package common.ontology.dsl.operation

object Warehouse {

    enum class Target {
        WAREHOUSE
    }

    data class Info(val target: Target)

    fun info(target: Target) = Info(target)
}