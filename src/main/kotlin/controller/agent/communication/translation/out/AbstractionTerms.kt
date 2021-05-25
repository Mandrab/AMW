package controller.agent.communication.translation.out

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Command.Command
import common.ontology.dsl.abstraction.Description.Description
import common.ontology.dsl.abstraction.Email.Email
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Name.Name
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity
import common.ontology.dsl.abstraction.Rack.Rack
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.abstraction.Shelf.Shelf
import common.ontology.dsl.abstraction.Status.Status
import common.ontology.dsl.abstraction.User.User
import controller.agent.communication.translation.out.Literals.get
import controller.agent.communication.translation.out.Literals.invoke
import controller.agent.communication.translation.out.Literals.toStringTerm
import controller.agent.communication.translation.out.Literals.toTerm
import jason.asSyntax.Literal

/**
 * Groups parsing functionalities for outgoing messages
 * It define the expected format for each outgoing message
 *
 * @author Paolo Baldini
 */
object AbstractionTerms {

    fun Address.term(): Literal = "address"(address.toStringTerm())

    fun Client.term(): Literal = "client"(name.toStringTerm())

    fun Command.term(): Literal = "command"(id.term(), name.term(), description.term())[script.term()]

    fun Description.term(): Literal = "description"(name.toStringTerm())

    fun Email.term(): Literal = "email"(address.toStringTerm())

    fun ID.term(): Literal = "id"(name.toStringTerm())

    fun WarehouseItem.term(): Literal = "item"(id.term(), position.term())

    fun QuantityItem.term(): Literal = "item"(id.term(), quantity.term())

    fun Name.term(): Literal = "name"(name.toStringTerm())

    fun Position.term(): Literal = "position"(rack.term(), shelf.term(), quantity.term())

    fun Quantity.term(): Literal = "quantity"(value)

    fun Rack.term(): Literal = "rack"(id)

    fun Script.term(): Literal = "script"(script.toStringTerm())

    fun Shelf.term(): Literal = "shelf"(id)

    fun Status.term(): Literal = "status"(state.value.toTerm())

    fun User.term(): Literal = "user"(client.term(), email.term(), address.term())
}
