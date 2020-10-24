package common.ontology.dsl.abstraction

object Client {

    data class Client(val name: String)

    fun client(name: String) = Client(name)
}