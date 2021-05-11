package tester.asl.robot_picker

import framework.ASLAgent
import framework.Framework.test
import org.junit.Test
import jade.lang.acl.ACLMessage.*
import org.junit.Assert

/**
 * Test class for RobotPicker's item retrieval
 *
 * @author Paolo Baldini
 */
class RetrieveItemTest {
    private val waitingTime = 2000L

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun itemPickRequestShouldBeAcceptedByRobot() = test {
        val pickerAID = agent("robot_picker", ASLAgent::class.java).aid
        val result = agent().sendRequest("retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))",
            pickerAID, INFORM).blockingReceive(waitingTime)

        assert(result, CONFIRM, "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))")
    }

    @Test fun justOneOutOfTwoItemPickRequestShouldBeAcceptedByRobot() = test {
        val pickerAID = agent("robot_picker", ASLAgent::class.java).aid
        val client = agent()

        client.sendRequest("retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))", pickerAID, INFORM)
        client.sendRequest("retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))", pickerAID, INFORM)
        val result1 = client.blockingReceive(waitingTime)
        val result2 = client.blockingReceive(waitingTime)

        assert(result1, CONFIRM, "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))")
        assert(result2, FAILURE, "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))")
    }
}
