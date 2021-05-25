package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.operation.Command.AddCommand
import common.ontology.dsl.operation.Command.ExecuteCommand
import common.ontology.dsl.operation.Command.add
import common.ontology.dsl.operation.Command.execute
import common.ontology.dsl.operation.Item.AddItem
import common.ontology.dsl.operation.Item.add
import common.ontology.dsl.operation.Order.PlaceOrder
import common.ontology.dsl.operation.Order.InfoOrders
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.Item.RemoveItem
import common.ontology.dsl.operation.Item.remove
import controller.agent.communication.translation.`in`.OperationTerms.parse
import org.junit.Test

/**
 * Test for incoming message translation
 *
 * @author Paolo Baldini
 */
class OperationTermsTest {

    @Test fun testAddCommandParse() =
        assert(add(command(id("a0"), name("name"), description("description"))[script("script")])
                == AddCommand.parse(
            """add(command(id("a0"), name("name"), description("description"))[script("script")])"""
        ))

    @Test fun testAddItemParse() =
        assert(add(item(id("a0"), position(rack(0), shelf(1), quantity(2))))
                == AddItem.parse("add(item(id(\"a0\"), position(rack(0),shelf(1),quantity(2))))"))

    @Test fun testCommandParse() =
            assert(execute(id("a0")) == ExecuteCommand.parse("execute(id(a0))"))

    @Test fun testOrderParse() =
            assert(
                    order(
                            client("antonio"),
                            email("antonio@email"),
                            address("address")
                    )[
                            item(id("a0"), quantity(5))
                    ] == PlaceOrder.parse(
                            "order(client(antonio), email(antonio@email), address(address))[item(id(\"a0\"),quantity(5))]"
                    )
            )

    @Test fun testInfoParse() =
            assert(info(client("antonio"), email("antonio@email"))
                == InfoOrders.parse("info(client(antonio),email(antonio@email))"))

    @Test fun testRemoveItemParse() =
            assert(remove(item(id("a0"), quantity(5))) == RemoveItem.parse("remove(item(id(\"a0\"),quantity(5)))"))
}
