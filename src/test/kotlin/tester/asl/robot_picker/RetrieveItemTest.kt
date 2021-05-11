package tester.asl.robot_picker

import framework.Messaging.plus
import framework.Framework.ASL
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
class RetrieveItemTest {
    private val retrieveTime = 750L                                         // fake time: it is simulated

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun itemPickCompletionShouldBeNotifiedByRobot() = test {
        agent .. INFORM + "retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))" - "123" > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123"
    }

    @Test fun justOneOutOfTwoItemPickRequestShouldBeCarriedOnByRobot() = test {
        agent .. INFORM + "retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))" - "123" > ASL.robotPicker
        agent .. INFORM + "retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))" - "abc" > ASL.robotPicker

        agent < FAILURE + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "abc"

        Thread.sleep(retrieveTime)

        agent < CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123"
    }

    @Test fun robotShouldEnsureRetrievalConfirmationToBeDelivered() = test {
        agent .. INFORM + "retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))" - "123" > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123"

        Thread.sleep(retryTime)

        agent < CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123"
    }

    @Test fun robotShouldStopSendConfirmationAfterReceivingAResponse() = test {
        agent .. INFORM + "retrieve(position(rack(1), shelf(2), quantity(3)), point(pid1))" - "123" > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123"
        agent .. CONFIRM + "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))" - "123" > ASL.robotPicker

        Thread.sleep(retryTime)

        Assert.assertEquals(null, agent.blockingReceive(waitingTime))
    }
}
