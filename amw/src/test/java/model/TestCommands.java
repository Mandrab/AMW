package model;

import asl_actions.load_commands;
import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import model.agents.TerminalAgent;
import model.agents.AgentInterface;
import model.agents.AgentInterfaceImpl;
import model.agents.admin.AdminAgentImpl;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.*;
import static interpackage.utils.Utils.sleep;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.fail;

public class TestCommands {

	private static final String SCRIPT_PATH = "src" + File.separator + "test" + File.separator + "asl" + File.separator;
	private static final String TEST_SCRIPT_01 = "cidScriptTest01-vidTestVersionID.asl";
	private static final long MAX_WAIT = 10000;
	private static final int TICK_TIME = 500;

	private boolean started;

	/*@Test
	public void scriptSubmission( ) throws IOException {
		// start system
		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );    // create communication dispatcher
		startAgent( dispatcher );                                           // start client agent

		// get test file data
		File file = new File( new java.io.File( "." ).getCanonicalPath( ) + File.separator + SCRIPT_PATH
				+ TEST_SCRIPT_01 );

		List<String> data = new LinkedList<>( Collections.singletonList( load_commands.getScript( file ) ) );   // get script
		System.out.println( data.get( 0 ) );
		data.addAll( load_commands.getRequirements( file ) );               // get script requirements

		// exec script
		dispatcher.agent.askFor( EXEC_SCRIPT, data.toArray( new String[ ] { } ) );

		sleep( TICK_TIME );

		dispatcher.agent.askFor( END );
	}*/

	@Test
	public void commandSubmission( ) {

		RequestDispatcherImpl dispatcher = new RequestDispatcherImpl( );

		startAgent( dispatcher );

		dispatcher.agent.askFor( EXEC_COMMAND, "cid0.0.0.1" );

		sleep( 15 * TICK_TIME );

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
