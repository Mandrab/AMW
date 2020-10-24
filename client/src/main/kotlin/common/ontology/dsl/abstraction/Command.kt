package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Description.Description
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Name.Name

object Command {

    data class Command(val id: ID, val name: Name, val description: Description)

    fun command(id: ID, name: Name, description: Description) = Command(id, name, description)
}