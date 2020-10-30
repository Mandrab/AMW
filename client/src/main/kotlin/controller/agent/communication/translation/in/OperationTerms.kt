package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.operation.AddCommand
import common.ontology.dsl.operation.AddItem
import common.ontology.dsl.operation.AddVersion
import common.ontology.dsl.operation.Execute
import common.ontology.dsl.operation.Order
import common.ontology.dsl.operation.RemoveItem
import common.ontology.dsl.operation.RetrieveOrder
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import controller.agent.communication.translation.`in`.AbstractionTerms.parseQuantityItem
import controller.agent.communication.translation.`in`.AbstractionTerms.parseWarehouseItem
import controller.agent.communication.translation.`in`.LiteralParser.asList

object OperationTerms {

    fun AddCommand.parse(string: String): AddCommand.AddCommand {
        val pattern = """add\((.*)\)""".toRegex()
        return add(Command.parse(pattern.find(string.trim())!![0]))
    }

    fun AddItem.parse(string: String): AddItem.AddItem {
        val pattern = """add\((.*)\)""".toRegex()
        return add(Item.parseWarehouseItem(pattern.find(string.trim())!![0]))
    }

    fun AddVersion.parse(string: String): AddVersion.AddVersion {
        val pattern = """add\((.*),variant\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return add(results[0], Variant.parse(results[1]))
    }

    fun Execute.parseCommand(string: String): Execute.ExecuteCommand  {
        val pattern = """execute\((.*)\)""".toRegex()
        return execute(ID.parse(pattern.find(string.trim())!![0], "command_id"))
    }

    fun Execute.parseScript(string: String): Execute.ExecuteScript {
        val pattern = """execute\((.*)\)""".toRegex()
        return execute(Script.parse(pattern.find(string.trim())!![0]))
    }

    fun Order.parseOrder(string: String): Order.Order {
        val pattern = """order\(client\((.*)\),email\((.*)\),address\((.*)\)\)\[(.*)]""".toRegex()
        val results = pattern.find(string.trim())!!
        return order(client(results[0]), email(results[1]), address(results[2]))[
                results[3].asList().map { Item.parseQuantityItem(it) }
        ]
    }

    fun Order.parseInfo(string: String): Order.OrderInfo {
        val pattern = """order\(client\((.*)\),email\((.*)\)\)""".toRegex()
        val results = pattern.find(string.trim())!!
        return info(client(results[0]), email(results[1]))
    }

    fun RemoveItem.parse(string: String): RemoveItem.RemoveItem {
        val pattern = """remove\((.*)\)""".toRegex()
        return remove(Item.parseWarehouseItem(pattern.find(string.trim())!![0]))
    }

    fun RetrieveOrder.parse(string: String): RetrieveOrder.RetrieveOrder {
        val pattern = """retrieve\((.*)\)\[(.*)]""".toRegex()
        val results = pattern.find(string.trim())!!
        return retrieve(ID.parse(results[0], "order_id"))[
                results[1].asList().map { Item.parseQuantityItem(it) }
        ]
    }

    private operator fun MatchResult.get(index: Int) = this.groupValues[index + 1]
}