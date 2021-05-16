package common.ontology.dsl.abstraction

object ID {

    data class ID(val name: String)

    fun id(id: String) = ID(id)
}
