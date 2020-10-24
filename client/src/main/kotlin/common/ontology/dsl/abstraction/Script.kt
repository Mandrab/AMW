package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Requirement.Requirement

object Script {

    data class Script(val script: String) {
        var requirements: List<Requirement>? = null
            private set

        operator fun get(vararg requirements: Requirement) = get(requirements.toList())

        operator fun get(requirements: List<Requirement>) = apply { this.requirements = requirements }

        operator fun plusAssign(requirement: Requirement) {
            requirements = requirements?.let { it + requirement } ?: listOf(requirement)
        }
    }

    fun script(script: String) = Script(script)
}