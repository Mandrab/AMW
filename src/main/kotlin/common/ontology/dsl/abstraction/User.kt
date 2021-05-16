package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email

object User {

	data class User(val client: Client, val email: Email, val address: Address)

	fun user(client: Client, email: Email, address: Address) = User(client, email, address)
}
