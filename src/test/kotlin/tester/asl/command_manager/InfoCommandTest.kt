package tester.asl.command_manager

import framework.Framework.test
import framework.Framework.Utility.agent
import common.ontology.dsl.abstraction.Address.address
import common.ontology.dsl.abstraction.Client.client
import common.ontology.dsl.abstraction.Email.email
import common.ontology.dsl.abstraction.ID.id
import common.ontology.dsl.abstraction.Item.item
import common.ontology.dsl.abstraction.Quantity.quantity
import common.ontology.dsl.operation.Order.info
import common.ontology.dsl.operation.Order.order
import controller.agent.communication.translation.out.OperationTerms.term
import framework.AMWSpecificFramework.ASL
import framework.Messaging.compareTo
import framework.Messaging.plus
import framework.Messaging.rangeTo
import jade.lang.acl.ACLMessage.*
import org.junit.Test
import org.junit.Assert

/**
 * Test class for CommandManager's command info request
 *
 * @author Paolo Baldini
 */
class InfoCommandTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun infoRequestShouldReturnAListOfCommands() = test {
        agent .. REQUEST + "info(commands)" > ASL.commandManager
        agent <= INFORM + ("""[command(id("Command1"),name("command 1 name"),description("description command 1"))[""" +
            """script("[{@l1 +!main <- .println('Executing script ...');.wait(500);!b}, """ +
            """{@l2 +!b <- .println('Script executed')}]")]]""")
    }
}
