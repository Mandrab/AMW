package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID

object Command {

    data class AddCommand(val command: Command) { companion object }

    data class ExecuteCommand(val commandId: ID.ID) { companion object }

    fun add(command: Command) = AddCommand(command)

    fun execute(commandId: ID.ID) = ExecuteCommand(commandId)
}
