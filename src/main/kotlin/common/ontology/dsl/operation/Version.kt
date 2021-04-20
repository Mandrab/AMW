package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Variant.Variant

object Version {

    data class AddVersion(val commandId: String, val variant: Variant) { companion object }

    fun add(commandId: String, variant: Variant) = AddVersion(commandId, variant)
}