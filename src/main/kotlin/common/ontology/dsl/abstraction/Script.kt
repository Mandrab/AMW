package common.ontology.dsl.abstraction

object Script {

    data class Script(val script: String)

    fun script(script: String) = Script(script)
}
