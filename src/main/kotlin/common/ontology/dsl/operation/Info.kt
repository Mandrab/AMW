package common.ontology.dsl.operation

object Info {

    enum class Target {
        WAREHOUSE,
        COMMANDS
    }

    data class Info(val target: Target)

    fun info(target: Target) = Info(target)
}
