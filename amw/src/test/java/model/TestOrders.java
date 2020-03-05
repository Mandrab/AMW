package model;

import interpackage.Item;
import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import model.agents.TerminalAgent;
import model.agents.AgentInterface;
import model.agents.AgentInterfaceImpl;
import model.agents.client.ClientAgentImpl;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.util.function.BiConsumer;

import static interpackage.RequestHandler.Request.*;
import static interpackage.utils.Utils.sleep;
import static org.junit.Assert.*;

public class TestOrders {

	private static final long MAX_WAIT = 10000;
	private static final int TICK_TIME = 500;

	private enum MailResult {
		CONFIRM,
		ERROR_NOT_FOUND,
		ERROR_CONFLICT
	}

	private boolean started;

	/*@Test
	public void itemNotFound( ) {

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		dispatcher.agent.askFor( ORDER, "User", "mail@mail.com", "Street xyz, 123", "\"Item " + Math.random( ) + "\"" );

		sleep( TICK_TIME );

		if ( ! checkMail( MailResult.ERROR_NOT_FOUND ) ) fail( "An error was expected" );

		dispatcher.agent.askFor( END );
	}*/

	@Test
	public void orderConfirm( ) {
		String[] items = new String[] { "\"Item 1\"", "\"Item 3\"" };

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		//pushItems( items );TODO

		//sleep( 10000 );

		dispatcher.agent.askFor( ORDER, "User", "mail@mail.com", "Street xyz, 123", items[ 0 ], items[ 0 ], items[ 1 ] );

		sleep( TICK_TIME );

		if ( ! checkMail( MailResult.CONFIRM ) ) fail( "A confirm was expected" );

		dispatcher.agent.askFor( END );
	}

	private void startAgent( RequestDispatcherImpl dispatcher ) {
		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( dispatcher );
				agent.start( ClientAgentImpl.class, true );
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

	private boolean checkMail( MailResult expectedResult ) {
		// TODO
		return false;
	}

	private static class RequestDispatcherImpl implements RequestDispatcher {

		private BiConsumer<Request, String[]> consumer;

		public TerminalAgent agent;

		public RequestDispatcherImpl( ) { }

		public RequestDispatcherImpl( BiConsumer<Request, String[]> consumer ) {
			this.consumer = consumer;
		}

		public void setConsumer ( BiConsumer<Request, String[]> consumer ) {
			this.consumer = consumer;
		}

		@Override
		public void register ( RequestHandler handler ) {
			if ( handler instanceof TerminalAgent )
				agent = ( TerminalAgent ) handler;
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
