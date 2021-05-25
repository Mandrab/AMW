package common.ontology.dsl.abstraction

/**
 * Represents 'client' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Client {

    data class Client(val name: String)

    fun client(name: String) = Client(name)
}
