package common.ontology.dsl.operation

import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email

object OrderInfo {

    data class OrderInfo(val client: Client, val email: Email)

    fun info(client: Client, email: Email) = OrderInfo(client, email)
}