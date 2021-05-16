package common.ontology.dsl.abstraction

object Quantity {

    data class Quantity(val value: Int)

    fun quantity(value: Int) = Quantity(value)
}
