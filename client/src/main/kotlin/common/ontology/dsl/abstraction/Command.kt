package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Description.Description
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Name.Name
import common.ontology.dsl.abstraction.Variant.Variant

object Command {

    data class CommandInfo(val id: ID, val name: Name, val description: Description)

    data class CommandImplementations(
            val id: ID,
            val name: Name,
            val description: Description,
            val variants: List<Variant>
    )

    fun command(id: ID, name: Name, description: Description) = CommandInfo(id, name, description)

    fun command(id: ID, name: Name, description: Description, variants: List<Variant>) =
            CommandImplementations(id, name, description, variants)
}