package model;

import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import model.agents.TerminalAgent;
import model.agents.AgentInterface;
import model.agents.AgentInterfaceImpl;
import model.agents.admin.AdminAgentImpl;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.util.function.BiConsumer;

import static interpackage.RequestHandler.Request.*;
import static interpackage.utils.Utils.sleep;
import static org.junit.Assert.fail;

public class TestCommands {

	private static final long MAX_WAIT = 10000;
	private static final int TICK_TIME = 500;

	private boolean started;

	/*@Test
	public void scriptSubmission( ) {
		String script = "[{" +
						"+!main <- .println( \"Executing script ...\" );\n" +
						".wait( 5000 );                                              // fake execution time\n" +
						"!b" +
				"}, {" +
						"+!b <- .println( \"Script executed\" )" +
				"}]";

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		dispatcher.agent.askFor( EXEC_SCRIPT, script, "\"req1\"", "\"req2\"", "\"req3\"" );

		sleep( TICK_TIME );

		dispatcher.agent.askFor( END );
	}*/

	@Test
	public void commandSubmission( ) {

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		dispatcher.agent.askFor( EXEC_COMMAND, "Command1" );

		sleep( 10 * TICK_TIME );

		dispatcher.agent.askFor( END );
	}

	private void startAgent( RequestDispatcherImpl dispatcher ) {
		new Thread( ( ) -> {
			try {
				AgentInterface agent = new AgentInterfaceImpl( dispatcher );
				agent.start( AdminAgentImpl.class, true );
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
