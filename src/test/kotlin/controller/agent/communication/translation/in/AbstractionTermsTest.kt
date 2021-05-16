package controller.agent.communication.translation.`in`

import common.ontology.dsl.abstraction.*
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Command.command
import common.ontology.dsl.abstraction.Description.description
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.Product
import common.ontology.dsl.abstraction.Item.QuantityItem
import common.ontology.dsl.abstraction.Item.WarehouseItem
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Name.name
import common.ontology.dsl.abstraction.Position.position
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.abstraction.Rack.rack
import common.ontology.dsl.abstraction.Requirement.requirement
import common.ontology.dsl.abstraction.Script.script
import common.ontology.dsl.abstraction.Shelf.shelf
import common.ontology.dsl.abstraction.Status
import common.ontology.dsl.abstraction.Status.States.CHECKING
import common.ontology.dsl.abstraction.Status.States.RETRIEVING
import common.ontology.dsl.abstraction.Status.status
import common.ontology.dsl.abstraction.User.user
import controller.agent.communication.translation.`in`.AbstractionTerms.parse
import org.junit.Test

class AbstractionTermsTest {

    @Test fun testAddressParse() =
            assert(address("via xyz n° 468") == Address.parse("address(\"via xyz n° 468\")"))

    @Test fun testClientParse() = assert(client("antonio") == Client.parse("client(\"antonio\")"))

    @Test fun testCommandParse() =
            assert(command(id("a0"),name("command"),description("description"))
                    == Command.parse("command(id(\"a0\"),name(\"command\"), description(\"description\"))"))

    @Test fun testDescriptionParse() =
            assert(description("description") == Description.parse("description(\"description\")"))

    @Test fun testEmailParse() = assert(email("antonio@email") == Email.parse("email(\"antonio@email\")"))

    @Test fun testIDParse() = assert(id("a0") == ID.parse("id(a0)"))

    @Test fun testOrderIDParse() = assert(id("a0") == ID.parse("order_id(a0)"))

    @Test fun testCommandIDParse() = assert(id("a0") == ID.parse("command_id(a0)"))

    @Test fun testVersionIDParse() = assert(id("a0") == ID.parse("v_id(a0)"))

    @Test fun testWarehouseItemParse() =
            assert(item(id("a0"),position(rack(0),shelf(1),quantity(2)))
                    == WarehouseItem.parse("item(id(\"a0\"), position(rack(0),shelf(1), quantity(2)))"))

    @Test fun testQuantityItemParse() =
            assert(item(id("a0"))[position(rack(5), shelf(6), quantity(7))]
                    == Product.parse("item(id(\"a0\"))[position(rack(5), shelf(6), quantity(7))]"))

    @Test fun testProductParse() =
            assert(item(id("a0"), quantity(5)) == QuantityItem.parse("item(id(\"a0\"), quantity(5))"))

    @Test fun testNameParse() = assert(name("name") == Name.parse("name(\"name\")"))

    @Test fun testPositionParse() =
            assert(position(rack(0),shelf(1), quantity(2))
                    == Position.parse("position(rack(0),shelf(1), quantity(2))"))

    @Test fun testQuantityParse() = assert(quantity(5) == Quantity.parse("quantity(5)"))

    @Test fun testRackParse() = assert(rack(5) == Rack.parse("rack(5)"))

    @Test fun testRequirementParse() = assert(requirement("a0") == Requirement.parse("a0"))

    @Test fun testScriptParse() = assert(script("x y z !") == Script.parse("script(x y z !)[]"))

    @Test fun testScriptWithRequirementsParse() =
            assert(script("x y z !")[requirement("r0"), requirement("r1")]
                    == Script.parse("script(x y z !)[r0, r1]"))

    @Test fun testShelfParse() = assert(shelf(5) == Shelf.parse("shelf(5)"))

    @Test fun testStatusCheckParse() = assert(status(CHECKING) == Status.parse("status(check)"))

    @Test fun testStatusRetrieveParse() = assert(status(RETRIEVING) == Status.parse("status(retrieve)"))

    @Test fun testUserParse() =
            assert(user(client("antonio"), email("antonio@mail"), address("address"))
                    == User.parse("user(client(\"antonio\"), email(\"antonio@mail\"), address(\"address\"))"))
}
