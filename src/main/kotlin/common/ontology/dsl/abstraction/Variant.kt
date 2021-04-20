package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Requirement.Requirement
import common.ontology.dsl.abstraction.Script.Script

object Variant {

    data class Variant(val id: ID, val script: Script, val requirements: List<Requirement>)

    fun variant(id: ID, script: Script, requirements: List<Requirement>) = Variant(id, script, requirements)
}