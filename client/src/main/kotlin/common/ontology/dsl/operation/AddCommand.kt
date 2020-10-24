package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Command.CommandInfo

object AddCommand {

    data class AddCommand(val command: CommandInfo)

    fun add(command: CommandInfo) = AddCommand(command)
}