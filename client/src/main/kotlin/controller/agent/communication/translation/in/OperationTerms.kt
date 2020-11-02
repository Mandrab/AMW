package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.operation.Command.AddCommand
import common.ontology.dsl.operation.Command.ExecuteCommand
import common.ontology.dsl.operation.Command.execute
import common.ontology.dsl.operation.Command.add
import common.ontology.dsl.operation.Item.AddItem
import common.ontology.dsl.operation.Item.RemoveItem
import common.ontology.dsl.operation.Item.remove
import common.ontology.dsl.operation.Item.add
import common.ontology.dsl.operation.Version.AddVersion
import common.ontology.dsl.operation.Version.add
import common.ontology.dsl.operation.Order.PlaceOrder
import common.ontology.dsl.operation.Order.InfoOrders
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.Script.ExecuteScript
import common.ontology.dsl.operation.Script.execute
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import controller.agent.communication.translation.`in`.LiteralParser.asList

object OperationTerms {

    fun AddCommand.Companion.parse(string: String): AddCommand = string.parse("""add\((.*)\)""")
            .run { add(Command.parse(next())) }

    fun AddItem.Companion.parse(string: String): AddItem = string.parse("""add\((.*)\)""")
            .run { add(WarehouseItem.parse(next())) }

    fun AddVersion.Companion.parse(string: String): AddVersion = string.parse("""add\((.*), ?variant\((.*)\)\)""")
            .run { add(next(), Variant.parse("variant(${next()})")) }

    fun ExecuteCommand.Companion.parse(string: String): ExecuteCommand = string.parse("""execute\((.*)\)""")
            .run { execute(ID.parse(next(), "command_id")) }

    fun ExecuteScript.Companion.parse(string: String): ExecuteScript = string.parse("""execute\((.*)\)""")
            .run { execute(Script.parse(next())) }

    fun PlaceOrder.Companion.parse(string: String): PlaceOrder =
            string.parse("""order\(client\((.*)\), ?email\((.*)\), ?address\((.*)\)\)\[(.*)]""")
                    .run { order(client(next()), email(next()), address(next()))[
                            next().asList().map { QuantityItem.parse(it) }
                    ] }

    fun InfoOrders.Companion.parse(string: String): InfoOrders =
            string.parse("""info\(client\((.*)\), ?email\((.*)\)\)""").run { info(client(next()), email(next())) }

    fun RemoveItem.Companion.parse(string: String): RemoveItem = string.parse("""remove\((.*)\)""")
            .run { remove(QuantityItem.parse(next())) }

    private fun String.parse(pattern: String) = pattern.toRegex().find(trim())!!.groupValues.drop(1).iterator()
}