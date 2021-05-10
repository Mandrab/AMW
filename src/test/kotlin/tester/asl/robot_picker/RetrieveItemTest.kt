package tester.asl.robot_picker

import common.ASLAgent
import common.Framework.Companion.test
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for RobotPicker's item retrieval
 *
 * @author Paolo Baldini
 */
class RetrieveItemTest {
    private val waitingTime = 1500L

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun itemPickRequestShouldBeAcceptedByRobot() = test {
        val pickerAID = agent("robot_picker", ASLAgent::class.java).aid
        val result = agent().sendRequest("retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))",
            pickerAID, INFORM).blockingReceive(waitingTime)

        assert(result, CONFIRM, "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))")
    }
}
