package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Address
import common.ontology.dsl.abstraction.Client
import common.ontology.dsl.abstraction.Command
import common.ontology.dsl.abstraction.Description
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.Email
import common.ontology.dsl.abstraction.ID
import common.ontology.dsl.abstraction.ID.ID as IDClass
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item
import common.ontology.dsl.abstraction.Name
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position
import common.ontology.dsl.abstraction.Quantity
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Requirement
import common.ontology.dsl.abstraction.Script
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.abstraction.Variant

object AbstractionTerms {

    fun Address.parse(string: String): Address.Address {
        val pattern = """address\((.*)\)""".toRegex()
        return address(pattern.find(string.trim())!![0])
    }

    fun Client.parse(string: String): Client.Client {
        val pattern = """client\((.*)\)""".toRegex()
        return client(pattern.find(string.trim())!![0])
    }

    fun Command.parse(string: String): Command.CommandInfo {
        val pattern = """command\(id\((.*)\), ?name\((.*)\), ?description\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return command(id(results[0]),name(results[1]),description(results[2]))
    }

    fun Description.parse(string: String): Description.Description {
        val pattern = """description\((.*)\)""".toRegex()
        return description(pattern.find(string.trim())!![0])
    }

    fun Email.parse(string: String): Email.Email {
        val pattern = """email\((.*)\)""".toRegex()
        return email(pattern.find(string.trim())!![0])
    }

    fun ID.parse(string: String, syntax: String = "id"): ID.ID {
        val pattern = """$syntax\((.*)\)""".toRegex()
        return IDClass(pattern.find(string.trim())!![0], syntax)
    }

    fun Item.parseWarehouseItem(string: String): Item.WarehouseItem {
        val pattern = """item\(id\((.*)\), ?position\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return item(id(results[0]),Position.parse("position(${results[1]})"))
    }

    fun Item.parseQuantityItem(string: String): Item.QuantityItem {
        val pattern = """item\(id\((.*)\), ?(.*)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return item(id(results[0]),Quantity.parse(results[1]))
    }

    fun Name.parse(string: String): Name.Name {
        val pattern = """name\((.*)\)""".toRegex()
        return name(pattern.find(string.trim())!![0])
    }

    fun Position.parse(string: String): Position.Position {
        val pattern = """position\(rack\((.*)\), ?shelf\((.*)\), ?quantity\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return position(rack(results[0].toInt()),shelf(results[1].toInt()),quantity(results[2].toInt()))
    }

    fun Quantity.parse(string: String): Quantity.Quantity {
        val pattern = """quantity\((.*)\)""".toRegex()
        return quantity(pattern.find(string.trim())!![0].toInt())
    }

    fun Rack.parse(string: String): Rack.Rack {
        val pattern = """rack\((.*)\)""".toRegex()
        return rack(pattern.find(string.trim())!![0].toInt())
    }

    fun Requirement.parse(string: String): Requirement.Requirement {
        val pattern = """(.*)""".toRegex()
        return requirement(pattern.find(string.trim())!![0])
    }

    fun Script.parse(string: String): Script.Script {
        val pattern = """script\((.*)\)\[(.*)]""".toRegex()
        return script(pattern.find(string.trim())!![0])[
                pattern.find(string.trim())!![1].split(",").map { Requirement.parse(it) }
        ]
    }

    fun Shelf.parse(string: String): Shelf.Shelf {
        val pattern = """shelf\((.*)\)""".toRegex()
        return shelf(pattern.find(string.trim())!![0].toInt())
    }

    fun Variant.parse(string: String): Variant.Variant {
        val pattern = """variant\(id\((.*)\), ?requirements\[(.*)], ?script\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return variant(
                id(results[0]),
                script(results[2]),
                results[1].split(",").map { Requirement.parse(it) }
        )
    }

    private operator fun MatchResult.get(index: Int) = this.groupValues[index + 1]
}