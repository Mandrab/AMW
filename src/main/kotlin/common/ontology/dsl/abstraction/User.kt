package common.ontology.dsl.abstraction

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Email.Email

/**
 * Represents 'user' abstraction in the system
 * Other abstractions can relate to it
 * The function(s) is(are) intended to create a DSL for the system
 *
 * @author Paolo Baldini
 */
object User {

	data class User(val client: Client, val email: Email, val address: Address)

	fun user(client: Client, email: Email, address: Address) = User(client, email, address)
}
