package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Command.Command

object AddCommand {

    data class AddCommand(val command: Command)

    fun add(command: Command) = AddCommand(command)
}