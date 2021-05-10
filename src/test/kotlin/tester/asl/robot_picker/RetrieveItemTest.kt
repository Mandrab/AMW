package tester.asl.robot_picker

import common.ASLAgent
import common.Framework.Companion.waitingTime
import common.Framework.Companion.test
import jade.lang.acl.ACLMessage
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for RobotPicker's item retrieval
 *
 * @author Paolo Baldini
 */
class RetrieveItemTest {

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun itemPickRequestShouldBeAcceptedByRobot() = test {
        val pickerAID = agent("robot_picker", ASLAgent::class.java).aid
        val result = agent().sendRequest("retrieve(P, PID)", pickerAID, INFORM).blockingReceive(waitingTime)

        assert(result, CONFIRM, "retrieve(P,PID)")
    }
}
