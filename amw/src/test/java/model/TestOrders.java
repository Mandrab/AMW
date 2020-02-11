package model;

import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import model.agent.AgentInterface;
import model.agent.AgentInterfaceImpl;
import model.agent.ClientAgent;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import static interpackage.RequestHandler.Request.END;
import static interpackage.RequestHandler.Request.ORDER;
import static org.junit.Assert.*;

public class TestOrders {

	@Test
	public void itemOfOrderNotFound( ) throws InterruptedException {
		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( dispatcher );
				agent.start( true );
			} catch ( InitializationError initializationError ) {
				fail( "Error in agent initialization" );
			}
		} ).start( );

		try {
			Thread.sleep( 1000 );
			assertTrue( dispatcher.agent != null );
			dispatcher.agent.askFor( ORDER, "Paolo", "Via XYZ 123", "Item 0", "Item 2" );
			Thread.sleep( 1000 );
			dispatcher.agent.askFor( END );
			Thread.sleep( 20000 );
		} catch ( IllegalStateException e ) {
			fail( "Agent uninitialized" );
		}
	}

	@Test
	public void submitOrder( ) throws InterruptedException {
		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( dispatcher );
				agent.start( true );
			} catch ( InitializationError initializationError ) {
				fail( "Error in agent initialization" );
			}
		} ).start( );

		try {
			Thread.sleep( 1000 );
			assertTrue( dispatcher.agent != null );
			dispatcher.agent.askFor( ORDER, "Paolo", "Via XYZ 123", "Item 1", "Item 2" );
			Thread.sleep( 1000 );
			dispatcher.agent.askFor( END );
			Thread.sleep( 20000 );
		} catch ( IllegalStateException e ) {
			fail( "Agent uninitialized" );
		}
	}

	public class RequestDispatcherImpl implements RequestDispatcher {

		public ClientAgent agent;

		@Override
		public void register ( RequestHandler handler ) {
			if ( handler instanceof ClientAgent )
				agent = ( ClientAgent ) handler;
		}

		@Override
		public void unregister ( RequestHandler handler ) { }

		@Override
		public <T> T askFor ( Request request, String... args ) { return null; }

	}

}
