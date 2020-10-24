package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Script.Script

object Execute {

    data class ExecuteCommand(val commandId: ID)

    data class ExecuteScript(val script: Script)

    fun execute(script: Script) = ExecuteScript(script)

    fun execute(commandId: ID) = ExecuteCommand(commandId)
}