package controller.agent.communication.translation.out

import common.ontology.dsl.abstraction.Address.Address
import common.ontology.dsl.abstraction.Client.Client
import common.ontology.dsl.abstraction.Command.CommandInfo
import common.ontology.dsl.abstraction.Description.Description
import common.ontology.dsl.abstraction.Email.Email
import common.ontology.dsl.abstraction.ID.ID
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Name.Name
import common.ontology.dsl.abstraction.Position.Position
import common.ontology.dsl.abstraction.Quantity.Quantity
import common.ontology.dsl.abstraction.Rack.Rack
import common.ontology.dsl.abstraction.Requirement.Requirement
import common.ontology.dsl.abstraction.Script.Script
import common.ontology.dsl.abstraction.Shelf.Shelf
import common.ontology.dsl.abstraction.Status.Status
import common.ontology.dsl.abstraction.User.User
import common.ontology.dsl.abstraction.Variant.Variant
import controller.agent.communication.translation.out.Literals.get
import controller.agent.communication.translation.out.Literals.invoke
import controller.agent.communication.translation.out.Literals.toStringTerm
import controller.agent.communication.translation.out.Literals.toTerm
import jason.asSyntax.Literal

object AbstractionTerms {

    fun Address.term(): Literal = "address"(address.toStringTerm())

    fun Client.term(): Literal = "client"(name.toStringTerm())

    fun CommandInfo.term(): Literal = "command"(id.term(), name.term(), description.term())

    fun Description.term(): Literal = "description"(name.toStringTerm())

    fun Email.term(): Literal = "email"(address.toStringTerm())

    fun ID.term(syntax: String = "id"): Literal = syntax(name.toStringTerm())

    fun WarehouseItem.term(): Literal = "item"(id.term(), position.term())

    fun QuantityItem.term(): Literal = "item"(id.term(), quantity.term())

    fun Name.term(): Literal = "name"(name.toStringTerm())

    fun Position.term(): Literal = "position"(rack.term(), shelf.term(), quantity.term())

    fun Quantity.term() = "quantity"(value)

    fun Rack.term(): Literal = "rack"(id)

    fun Requirement.term() = name.toTerm()

    fun Script.term(): Literal = "script"(script.toStringTerm())
            .run { requirements?.let { this[it.map { r -> r.term() }] } ?: this }

    fun Shelf.term(): Literal = "shelf"(id)

    fun Status.term(): Literal = "status"(state.value.toTerm())

    fun User.term(): Literal = "user"(client.term(), email.term(), address.term())

    fun Variant.term(): Literal = "variant"(id.term("v_id"),
            "requirements".get(*requirements.map { it.term() }.toTypedArray()), script.term())
}