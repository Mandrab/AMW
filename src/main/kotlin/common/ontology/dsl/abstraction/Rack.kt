package common.ontology.dsl.abstraction

object Rack {

    data class Rack(val id: Int)

    fun rack(id: Int) = Rack(id)
}
