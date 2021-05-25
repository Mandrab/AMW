package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.ID

/**
 * Represents 'command' operation(s) in the system
 * It refer to ontology abstractions
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object Command {

    /**
     * Represents a request for command addition
     *
     * @author Paolo Baldini
     */
    data class AddCommand(val command: Command) { companion object }

    /**
     * Represents a request for command execution
     *
     * @author Paolo Baldini
     */
    data class ExecuteCommand(val id: ID.ID) { companion object }

    fun add(command: Command) = AddCommand(command)

    fun execute(commandId: ID.ID) = ExecuteCommand(commandId)
}
