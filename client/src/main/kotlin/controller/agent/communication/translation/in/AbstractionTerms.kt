package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.ID.v_id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Requirement.requirement
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.abstraction.Variant.variant
import controller.agent.communication.translation.`in`.LiteralParser.asList
import controller.agent.communication.translation.`in`.LiteralParser.queue
import controller.agent.communication.translation.`in`.LiteralParser.splitStructAndList
import controller.agent.communication.translation.`in`.LiteralParser.value

object AbstractionTerms {

    /**
     * Parse a string to extract a Version object
     */
    fun String.asVariant() =
            variant(
                v_id(value("v_id")!!),
                script(value("script")!!),
                value().asList().first { it.startsWith("requirements") }.queue().asList()
                        .map { requirement(it) }
            )

    /**
     * Parse a string to extract a Command object
     */
    fun String.asCommand() = splitStructAndList(this).run {
        command(
                id(first.value("id")!!),
                name(first.value("name")!!),
                description(first.value("description")!!),
                second.asList().filter { it.isNotBlank() }.map { it.asVariant() }
        )
    }

    /**
     * Parse a string to extract an Item object
     */
    fun String.asItem() = item(id(value("id")!!), value("reserved")!!.toInt(), queue().asList().map {
        position(
                rack(it.value("rack")!!.toInt()),
                shelf(it.value("shelf")!!.toInt()),
                quantity(it.value("quantity")!!.toInt()),
        )
    })
}