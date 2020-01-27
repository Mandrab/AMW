import Model.Agent.AgentInterface;
import Model.Agent.AgentInterfaceImpl;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import static Interpackage.RequestHandler.Request.END;
import static Interpackage.RequestHandler.Request.ORDER;
import static org.junit.Assert.*;

public class TestOrders {

	private AgentInterface agent;

	@Test
	public void itemOfOrderNotFound( ) throws InterruptedException {
		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( );
				agent.start( );
			} catch ( InitializationError initializationError ) {
				fail( "Error in agent initialization" );
			}
		} ).start( );

		try {
			Thread.sleep( 1000 );
			assertTrue( agent != null );
			agent.askFor( ORDER, "Paolo", "Via XYZ 123", "Item 0", "Item 2" );
			Thread.sleep( 1000 );
			agent.askFor( END );
			Thread.sleep( 20000 );
		} catch ( IllegalStateException e ) {
			fail( "Agent uninitialized" );
		}
	}

	@Test
	public void submitOrder( ) throws InterruptedException {
		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( );
				agent.start( );
			} catch ( InitializationError initializationError ) {
				fail( "Error in agent initialization" );
			}
		} ).start( );

		try {
			Thread.sleep( 1000 );
			assertTrue( agent != null );
			agent.askFor( ORDER, "Paolo", "Via XYZ 123", "Item 1", "Item 2" );
			Thread.sleep( 1000 );
			agent.askFor( END );
			Thread.sleep( 20000 );
		} catch ( IllegalStateException e ) {
			fail( "Agent uninitialized" );
		}
	}

}
