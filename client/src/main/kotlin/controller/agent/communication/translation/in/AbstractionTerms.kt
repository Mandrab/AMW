package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.ID.v_id
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Requirement.requirement
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Variant.variant
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.`in`.LiteralParser.queue
import controller.agent.communication.translation.`in`.LiteralParser.splitStructAndList
import controller.agent.communication.translation.`in`.LiteralParser.value

object AbstractionTerms {

    /**
     * Parse a string to extract a Version object
     */
    fun parseVersion(input: String) =
            variant(
                v_id(input.value("v_id")!!),
                script(input.value("script")!!),
                input.value().asList().first { it.startsWith("requirements") }.queue().asList()
                        .map { requirement(it) }
            )

    /**
     * Parse a string to extract a Command object
     */
    fun parseCommand(input: String) = splitStructAndList(input).run {
        command(
                id(first.value("id")!!),
                name(first.value("name")!!),
                description(first.value("description")!!),
                second.asList().filter { it.isNotBlank() }.map { parseVersion(it) }
        )
    }
}