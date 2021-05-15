package tester.asl.robot_picker

import framework.Messaging.plus
import framework.AMWSpecificFramework.ASL
import framework.AMWSpecificFramework.JADE
import framework.AMWSpecificFramework.mid
import framework.Framework.Utility.agent
import framework.Framework.test
import framework.Messaging.compareTo
import framework.Messaging.minus
import framework.Messaging.rangeTo
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for RobotPicker's item retrieval
 *
 * @author Paolo Baldini
 */
class ExecuteCommandTest {
    private val retrieveTime = 750L                                         // fake time: it is simulated

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun executionRequestShouldCauseRequestToCommanManager() = test { JADE.commandManager
        agent .. REQUEST + """command(id("Command1"))""" - "123" > ASL.robotPicker

        Thread.sleep(retrieveTime)

        JADE.commandManager <= REQUEST + """command(id("Command1"))[${mid(1)}]"""
    }
}
