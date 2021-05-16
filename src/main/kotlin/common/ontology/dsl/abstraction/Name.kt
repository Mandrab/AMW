package common.ontology.dsl.abstraction

object Name {

    data class Name(val name: String)

    fun name(name: String) = Name(name)
}
