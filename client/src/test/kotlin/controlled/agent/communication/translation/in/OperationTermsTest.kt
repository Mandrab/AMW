package controlled.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.command_id
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.ID.order_id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Requirement.requirement
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.abstraction.Variant.variant
import common.ontology.dsl.operation.*
import common.ontology.dsl.operation.AddCommand.add
import common.ontology.dsl.operation.AddItem.add
import common.ontology.dsl.operation.AddVersion.add
import common.ontology.dsl.operation.Execute.execute
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import common.ontology.dsl.operation.RemoveItem.remove
import common.ontology.dsl.operation.RetrieveOrder.retrieve
import controller.agent.communication.translation.`in`.OperationTerms.parse
import controller.agent.communication.translation.`in`.OperationTerms.parseCommand
import controller.agent.communication.translation.`in`.OperationTerms.parseInfo
import controller.agent.communication.translation.`in`.OperationTerms.parseOrder
import controller.agent.communication.translation.`in`.OperationTerms.parseScript
import org.junit.Test

class OperationTermsTest {

    @Test fun testAddCommandParse() =
            assert(add(command(id("a0"), name("name"), description("description")))
                    == AddCommand.parse("add(command(id(a0), name(name), description(description)))"))

    @Test fun testAddItemParse() =
        assert(add(item(id("a0"), position(rack(0), shelf(1), quantity(2))))
                == AddItem.parse("add(item(id(a0), position(rack(0),shelf(1),quantity(2))))"))

    @Test fun testAddVersionParse() =
            assert(
                add(
                        "id",
                        variant(id("a0"), script("x y z !"), listOf(requirement("r0"), requirement("r1")))
                ) == AddVersion.parse("add(id, variant(id(a0), requirements[r0, r1], script(x y z !)))")
            )

    @Test fun testCommandParse() =
            assert(execute(command_id("a0")) == Execute.parseCommand("execute(command_id(a0))"))

    @Test fun testScriptParse() = assert(execute(script("x y")) == Execute.parseScript("execute(script(x y)[])"))

    @Test fun testOrderParse() =
            assert(
                    order(
                            client("antonio"),
                            email("antonio@email"),
                            address("address")
                    )[
                            item(id("a0"), quantity(5))
                    ] == Order.parseOrder(
                            "order(client(antonio), email(antonio@email), address(address))[item(id(a0),quantity(5))]"
                    )
            )

    @Test fun testInfoParse() =
            assert(info(client("antonio"), email("antonio@email"))
                == Order.parseInfo("info(client(antonio),email(antonio@email))"))

    @Test fun testRemoveItemParse() =
            assert(remove(item(id("a0"),position(rack(5),shelf(6),quantity(7))))
                    == RemoveItem.parse("remove(item(id(a0),position(rack(5),shelf(6),quantity(7))))"))

    @Test fun testRemoveOrderParse() =
            assert(
                    retrieve(
                            order_id("a0")
                    )[
                            item(id("i0"),quantity(5)), item(id("i1"),quantity(5))
                    ] == RetrieveOrder.parse(
                            "retrieve(order_id(a0))[item(id(i0),quantity(5)), item(id(i1),quantity(5))]"
                    )
            )
}