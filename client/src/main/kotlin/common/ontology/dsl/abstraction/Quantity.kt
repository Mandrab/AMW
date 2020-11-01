package common.ontology.dsl.abstraction

object Quantity {

    data class Quantity(val value: Int)

    data class Reserved(val value: Int)

    fun quantity(value: Int) = Quantity(value)

    fun reserved(value: Int) = Reserved(value)
}