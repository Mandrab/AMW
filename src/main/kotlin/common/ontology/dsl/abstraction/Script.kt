package common.ontology.dsl.abstraction

/**
 * Represents 'script' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Script {

    data class Script(val script: String)

    fun script(script: String) = Script(script)
}
