package tester.asl.robot_picker

import framework.Messaging.plus
import framework.Framework.ASL
import framework.Framework.Utility.agent
import framework.Framework.Utility.mid
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
    private fun retrieveMessage(mid: Int) = "retrieve(position(rack(1),shelf(2),quantity(3)),point(pid1))[${mid(mid)}]"

    @Test fun testerIsRegistering() = test { oneshotAgent(Assert::assertNotNull) }

    @Test fun itemPickCompletionShouldBeNotifiedByRobot() = test {
        agent .. REQUEST + retrieveMessage(3) > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + retrieveMessage(3)
    }

    @Test fun justOneOutOfTwoItemPickRequestShouldBeCarriedOnByRobot() = test {
        agent .. REQUEST + retrieveMessage(3) > ASL.robotPicker
        agent .. REQUEST + retrieveMessage(4) > ASL.robotPicker

        agent < FAILURE + "error(${retrieveMessage(4)})"

        Thread.sleep(retrieveTime)

        agent < CONFIRM + retrieveMessage(3)
    }

    @Test fun robotShouldEnsureRetrievalConfirmationToBeDelivered() = test {
        agent .. REQUEST + retrieveMessage(3) > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + retrieveMessage(3)

        Thread.sleep(retryTime)

        agent < CONFIRM + retrieveMessage(3)
    }

    @Test fun robotShouldStopSendConfirmationAfterReceivingAResponse() = test {
        agent .. REQUEST + retrieveMessage(3) > ASL.robotPicker

        Thread.sleep(retrieveTime)

        agent < CONFIRM + retrieveMessage(3)
        agent .. CONFIRM + retrieveMessage(3) > ASL.robotPicker

        Thread.sleep(retryTime)

        Assert.assertEquals(null, agent.blockingReceive(waitingTime))
    }
}
