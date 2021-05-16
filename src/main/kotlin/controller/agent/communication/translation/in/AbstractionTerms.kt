package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.ID as IDClass
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Status.States.*
import common.ontology.dsl.abstraction.Shelf.shelf
import controller.agent.communication.translation.`in`.LiteralParser.asList

object AbstractionTerms {

    fun Address.parse(string: String): Address.Address = string.parse(
        """address\(\"(.*)\"\)"""
    ).run {
        address(next())
    }

    fun Client.parse(string: String): Client.Client = string.parse(
        """client\(\"(.*)\"\)"""
    ).run {
        client(next())
    }

    fun Command.parse(string: String): Command.Command = string.parse(
        """command\(id\(\"(.*)\"\), ?name\(\"(.*)\"\), ?description\(\"(.*)\"\)\)\[script\(\"(.*)\"\)\]"""
    ).run {
        command(id(next()),name(next()),description(next()))[script(next())]
    }

    fun Description.parse(string: String): Description.Description = string.parse(
        """description\(\"(.*)\"\)""")
    .run {
        description(next())
    }

    fun Email.parse(string: String): Email.Email = string.parse(
        """email\(\"(.*)\"\)"""
    ).run {
        email(next())
    }

    fun ID.parse(string: String): ID.ID = string.parse(
        """id\((.*)\)"""
    ).run {
        id(next())
    }

    fun Product.Companion.parse(string: String): Product = string.parse(
        """item\(id\("(.*)"\)\)\[(.*)]"""
    ).run {
        item(id(next()))[next().asList().map { Position.parse(it) }]
    }

    fun WarehouseItem.Companion.parse(string: String): WarehouseItem = string.parse(
        """item\(id\("(.*)"\), ?position\((.*)\)\)"""
    ).run {
        item(id(next()),Position.parse("position(${next()})"))
    }

    fun QuantityItem.Companion.parse(string: String): QuantityItem = string.parse(
        """item\(id\("(.*)"\), ?(.*)\)"""
    ).run {
        item(id(next()),Quantity.parse(next()))
    }

    fun Name.parse(string: String): Name.Name = string.parse(
        """name\(\"(.*)\"\)"""
    ).run {
        name(next())
    }

    fun Position.parse(string: String): Position.Position = string.parse(
    """position\(rack\((.*)\), ?shelf\((.*)\), ?quantity\((.*)\)\)"""
    ).run {
        position(rack(next().toInt()),shelf(next().toInt()),quantity(next().toInt()))
    }

    fun Quantity.parse(string: String): Quantity.Quantity = string.parse(
        """quantity\((.*)\)"""
    ).run {
        quantity(next().toInt())
    }

    fun Rack.parse(string: String): Rack.Rack = string.parse(
        """rack\((.*)\)"""
    ).run {
        rack(next().toInt())
    }

    fun Script.parse(string: String): Script.Script = string.parse(
        """script\((.*)\)"""
    ).run {
        script(next())
    }

    fun Shelf.parse(string: String): Shelf.Shelf = string.parse(
        """shelf\((.*)\)"""
    ).run {
        shelf(next().toInt())
    }

    fun Status.parse(string: String): Status.Status = string.parse(
        """status\((.*)\)"""
    ).run {
        status(when (next()) {
                CHECKING.value -> CHECKING
                else -> RETRIEVING
        })
    }

    fun User.parse(string: String): User.User = string.parse(
        """user\(client\(\"(.*)\"\), ?email\(\"(.*)\"\), ?address\(\"(.*)\"\)\)"""
    ).run {
        user(client(next()), email(next()), address(next()))
    }

    private fun String.parse(pattern: String) = pattern.toRegex().find(trim())!!.groupValues.drop(1).iterator()
}
