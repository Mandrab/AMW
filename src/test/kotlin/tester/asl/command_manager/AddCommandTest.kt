package tester.asl.command_manager

import framework.Framework.test
import framework.Framework.Utility.agent
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
class AddCommandTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun commandsAddRequestShouldFailIfInWrongFormat() = test {
        agent .. REQUEST + "add(command(id, name, description)[notScript(wrong)])" > ASL.commandManager
        agent <= FAILURE + "unknown(add(command(id,name,description)[notScript(wrong)]))"
    }
}
