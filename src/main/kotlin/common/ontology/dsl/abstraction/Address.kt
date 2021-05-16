package common.ontology.dsl.abstraction

object Address {

    data class Address(val address: String)

    fun address(address: String) = Address(address)
}
