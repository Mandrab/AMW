package common.ontology.dsl.abstraction

object Requirement {

    data class Requirement(val name: String)

    fun requirement(name: String) = Requirement(name)
}