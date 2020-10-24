package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Variant.Variant

object AddVersion {

    data class AddVersion(val commandId: String, val variant: Variant)

    fun add(commandId: String, variant: Variant) = AddVersion(commandId, variant)
}