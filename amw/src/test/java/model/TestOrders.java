package model;

import interpackage.Item;
import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import model.agent.AgentInterface;
import model.agent.AgentInterfaceImpl;
import model.agent.ClientAgent;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static interpackage.RequestHandler.Request.*;
import static interpackage.utils.Utils.sleep;
import static model.utils.LiteralUtils.getValue;
import static org.junit.Assert.*;

public class TestOrders {

	private static final long MAX_WAIT = 10000;
	private static final int TICK_TIME = 500;

	private boolean started;

	@Test
	public void itemNotFound( ) {

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		AtomicBoolean consumed = new AtomicBoolean( false );

		dispatcher.setConsumer( ( request, args ) -> {
			assertEquals( MANAGE_ERROR, request );
			assertEquals( "\"404, not found\"", getValue( args[ 1 ] ) );
			consumed.set( true );
		} );

		dispatcher.agent.askFor( ORDER, "User", "Street xyz, 123", "\"Item " + Math.random( ) + "\"" );

		sleep( TICK_TIME );

		if ( ! consumed.get( ) ) fail( "An error was expected" );

		dispatcher.agent.askFor( END );
	}

	@Test
	public void orderConfirm( ) {
		Item[] items = new Item[] { new Item( "\"Item 1\"", 5, 3, 2 ), new Item( "\"Item 3\"", 2, 5, 1 ) };

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		pushItems( items );

		AtomicBoolean consumed = new AtomicBoolean( false );

		dispatcher.setConsumer( ( request, args ) -> {
			assertEquals( CONFIRMATION, request );
			assertTrue( args[ 0 ].contains( "UserStreet xyz, 123" ) );
			assertTrue( args[ 1 ].contains( "order" ) );
			consumed.set( true );
		} );

		dispatcher.agent.askFor( ORDER, "User", "Street xyz, 123", items[ 0 ].getItemId( ), items[ 0 ].getItemId( ),
				items[ 1 ].getItemId( ) );

		sleep( TICK_TIME );

		if ( ! consumed.get( ) ) fail( "A confirm was expected" );

		dispatcher.agent.askFor( END );
	}

	private void startAgent( RequestDispatcherImpl dispatcher ) {
		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( dispatcher );
				agent.start( true );
			} catch ( InitializationError initializationError ) {
				fail( "Error in agent initialization" );
			} catch ( NullPointerException ignored ) {
				fail( "Error in connection to the system" );
			}
		} ).start( );

		int elapsedTime;
		for ( elapsedTime = 0; !started && dispatcher.agent == null && elapsedTime <= MAX_WAIT; elapsedTime += TICK_TIME )
			sleep( TICK_TIME );

		if ( dispatcher.agent == null ) fail( "Connection is taking too much time" );

		started = true;
	}

	private void pushItems( Item... items ) {
		// TODO push items to test
	}

	private static class RequestDispatcherImpl implements RequestDispatcher {

		private BiConsumer<Request, String[]> consumer;

		public ClientAgent agent;

		public RequestDispatcherImpl( ) { }

		public RequestDispatcherImpl( BiConsumer<Request, String[]> consumer ) {
			this.consumer = consumer;
		}

		public void setConsumer ( BiConsumer<Request, String[]> consumer ) {
			this.consumer = consumer;
		}

		@Override
		public void register ( RequestHandler handler ) {
			if ( handler instanceof ClientAgent )
				agent = ( ClientAgent ) handler;
		}

		@Override
		public void unregister ( RequestHandler handler ) { }

		@Override
		public <T> T askFor ( Request request, String... args ) {
			consumer.accept( request, args );
			return null;
		}

	}

}
