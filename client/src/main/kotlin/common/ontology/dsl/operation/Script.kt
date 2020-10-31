package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Script.Script

object Script {

    data class ExecuteScript(val script: Script) { companion object }

    fun execute(script: Script) = ExecuteScript(script)
}