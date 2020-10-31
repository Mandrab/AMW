package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
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
import controller.agent.communication.translation.`in`.AbstractionTerms.parseQuantityItem
import controller.agent.communication.translation.`in`.AbstractionTerms.parseWarehouseItem
import controller.agent.communication.translation.`in`.LiteralParser.asList

object OperationTerms {

    fun AddCommand.Companion.parse(string: String): AddCommand {
        val pattern = """add\((.*)\)""".toRegex()
        return add(Command.parse(pattern.find(string.trim())!![0]))
    }

    fun AddItem.Companion.parse(string: String): AddItem {
        val pattern = """add\((.*)\)""".toRegex()
        return add(Item.parseWarehouseItem(pattern.find(string.trim())!![0]))
    }

    fun AddVersion.Companion.parse(string: String): AddVersion {
        val pattern = """add\((.*), ?variant\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return add(results[0], Variant.parse("variant(${results[1]})"))
    }

    fun ExecuteCommand.Companion.parse(string: String): ExecuteCommand {
        val pattern = """execute\((.*)\)""".toRegex()
        return execute(ID.parse(pattern.find(string.trim())!![0], "command_id"))
    }

    fun ExecuteScript.Companion.parse(string: String): ExecuteScript {
        val pattern = """execute\((.*)\)""".toRegex()
        return execute(Script.parse(pattern.find(string.trim())!![0]))
    }

    fun PlaceOrder.Companion.parse(string: String): PlaceOrder {
        val pattern = """order\(client\((.*)\), ?email\((.*)\), ?address\((.*)\)\)\[(.*)]""".toRegex()
        val results = pattern.find(string.trim())!!
        return order(client(results[0]), email(results[1]), address(results[2]))[
                results[3].asList().map { Item.parseQuantityItem(it) }
        ]
    }

    fun InfoOrders.Companion.parse(string: String): InfoOrders {
        val pattern = """info\(client\((.*)\), ?email\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return info(client(results[0]), email(results[1]))
    }

    fun RemoveItem.Companion.parse(string: String): RemoveItem {
        val pattern = """remove\((.*)\)""".toRegex()
        return remove(Item.parseWarehouseItem(pattern.find(string.trim())!![0]))
    }

    private operator fun MatchResult.get(index: Int) = this.groupValues[index + 1]
}