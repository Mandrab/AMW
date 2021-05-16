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
class InfoCommandTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun commandsInfoRequestShouldReturnAListOfCommands() = test {
        agent .. REQUEST + "info(commands)" > ASL.commandManager
        agent <= INFORM + ("""[command(id("Command1"),name("command 1 name"),description("description command 1"))[""" +
            """script("[{@l0 +!main <- .println('Executing script ...');.wait(500);!b}, """ +
            """{@l1 +!b <- .println('Script executed')}]")]]""")
    }

    @Test fun commandInfoRequestShouldReturnAScript() = test {
        agent .. REQUEST + """command(id("Command1"))[mid(abc)]""" > ASL.commandManager
        agent <= INFORM + ("""script("[{@l0 +!main <- .println('Executing script ...');.wait(500);!b}, """ +
                """{@l1 +!b <- .println('Script executed')}]")""" + "[mid(abc)]")
    }

    @Test fun commandInfoOfNonExistingCommandShouldFail() = test {
        agent .. REQUEST + """command(id("Command 999"))""" > ASL.commandManager
        agent <= FAILURE + """unknown(command(id("Command 999")))"""
    }
}
