package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Description.Description
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Name.Name
import common.ontology.dsl.abstraction.Script.Script

object Command {

    data class Command(
            val id: ID,
            val name: Name,
            val description: Description
    ) {
        lateinit var script: Script

        operator fun get(script_: Script) = this.apply { script = script_ }
    }

    fun command(id: ID, name: Name, description: Description) = Command(id, name, description)
}
