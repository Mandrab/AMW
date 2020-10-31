package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Command.CommandInfo
import common.ontology.dsl.abstraction.ID

object Command {

    data class AddCommand(val command: CommandInfo) { companion object }

    data class ExecuteCommand(val commandId: ID.ID) { companion object }

    fun add(command: CommandInfo) = AddCommand(command)

    fun execute(commandId: ID.ID) = ExecuteCommand(commandId)
}