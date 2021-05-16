package common.ontology.dsl.abstraction

object Description {

    data class Description(val name: String)

    fun description(id: String) = Description(id)
}
