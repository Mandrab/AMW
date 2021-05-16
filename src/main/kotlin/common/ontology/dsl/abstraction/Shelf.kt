package common.ontology.dsl.abstraction

object Shelf {

    data class Shelf(val id: Int)

    fun shelf(id: Int) = Shelf(id)
}
