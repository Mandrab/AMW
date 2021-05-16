package common.ontology.dsl.abstraction

object Email {

    data class Email(val address: String)

    fun email(address: String) = Email(address)
}
