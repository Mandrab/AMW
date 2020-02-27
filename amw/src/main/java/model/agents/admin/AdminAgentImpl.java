package model.agents.admin;

import interpackage.Command;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import model.agents.client.ClientAgentImpl;
import model.utils.LiteralBuilder;
import model.utils.ServiceType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.INFO_COMMANDS;
import static jade.lang.acl.ACLMessage.*;
import static model.utils.LiteralBuilder.buildLiteral;
import static model.utils.LiteralParser.*;
import static model.utils.ServiceType.*;

public class AdminAgentImpl extends ClientAgentImpl {

	private static final int UPDATE_TIME = 1000;// TODO

	private List<Command> repositoryCommands;
	private long lastUpdate;

	@Override
	protected void setup ( ) {
		super.setup( );

		repositoryCommands = new LinkedList<>( );

		addBehaviour( updateCommands( ) );
	}

	protected Behaviour updateCommands ( ) {
		return new CyclicBehaviour( ) {
			@Override
			public void action ( ) {
				if ( new Date( ).getTime( ) - lastUpdate > UPDATE_TIME ) {          // update repository (commands) info
					// setup the message and send it
					Structure info = new Structure( "info" );
					info.addTerm( new Structure( "commands" ) );
					new MessageSender( ServiceType.MANAGEMENT_COMMANDS.getName( ), ServiceType.INFO_COMMANDS.getName( ),
							CFP, info ).require( getAgent( ) ).thenAccept( message -> {
						synchronized ( this ) {
							repositoryCommands = split( message.getContent( ) ).stream( ).map( Command::parse )
									.collect( Collectors.toList( ) );               // update repository info
						}
					} );
					lastUpdate = new Date( ).getTime( );
				}
				block( UPDATE_TIME );                                               // wait specified time
			}
		};
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T askFor ( Request request, String... args ) {

		if ( request == INFO_COMMANDS ) {
			CompletableFuture<List<Command>> result = new CompletableFuture<>( );
			addBehaviour( new OneShotBehaviour( ) {
				@Override
				public void action ( ) {
					synchronized ( this ) {
						result.complete( repositoryCommands.stream( ).map( Command::clone ).collect( Collectors.toList( ) ) );
					}
				}
			} );
			return ( T ) result;
		}

		if ( request == Request.EXEC_COMMAND ) {
			Literal execute = new LiteralBuilder( ).setName( "execute" ).addValues( LiteralBuilder.buildLiteral( "command_id", args[ 0 ] ) ).build( );

			new MessageSender( EXECUTOR_COMMAND.getName( ), EXEC_COMMAND.getName( ), CFP, execute ).require( this )
					.thenAccept( c -> {
						try {
							if ( c == null ) return;                                    // timeout
							if ( c.getPerformative( ) == REFUSE ) {                     // refuse to propose
								// TODO
							} else if ( c.getPerformative( ) == PROPOSE ) {             // accept
								new MessageSender( c.getSender( ), ACCEPT_PROPOSAL, c.getContentObject( ) ).send( this );
							}
						} catch ( UnreadableException e ) {
							e.printStackTrace( );
						}
					} );
			return null;
		}

		if ( request == Request.EXEC_SCRIPT ) {
			String script = args[ 0 ];

			Literal scriptLit = new LiteralBuilder( ).setName( "script" ).addValues( script )
					.addQueue( Arrays.copyOfRange( args, 1, args.length ) ).build( );

			Literal execute = new LiteralBuilder( ).setName( "execute" ).addValues( scriptLit ).build( );

			new MessageSender( EXECUTOR_SCRIPT.getName( ), EXEC_SCRIPT.getName( ), CFP, execute ).require( this )
					.thenAccept( c -> {
						if ( c == null ) return;                                    // timeout
						if ( c.getPerformative( ) == REFUSE ) {                     // refuse to propose
// TODO
						} else if ( c.getPerformative( ) == PROPOSE ) {             // accept
							new MessageSender( EXECUTOR_SCRIPT.getName( ), EXEC_SCRIPT.getName( ), ACCEPT_PROPOSAL,
									LiteralBuilder.buildLiteral( "execute", "script" ) ).setMsgID( c.getInReplyTo( ) )
									.send( this );
						}
					} );
			return null;
		}

		return super.askFor( request, args );
	}
}