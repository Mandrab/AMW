package model.agent;

import interpackage.Command;
import interpackage.Item;
import interpackage.RequestDispatcher;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import model.utils.ServiceType;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.*;
import static model.utils.LiteralUtils.*;

public class ClientAgentImpl extends Agent implements ClientAgent {

	private static final String NOT_IMPLEMENTED_REQUEST_MSG = "The return of the required element has not yet been " +
			"implemented. Sorry for the discomfort";
	private static final int UPDATE_TIME = 5000;// TODO
	private static final int RESPONSE_TIME = 50000;

	private RequestDispatcher dispatcher;
	private List<String> messages;
	private List<Item> warehouseItems;
	private List<Command> repositoryCommands;
	private long lastUpdate;

	@Override
	protected void setup( ) {
		dispatcher = ( RequestDispatcher ) getArguments( )[ 0 ];
		dispatcher.register( this );

		messages = new LinkedList<>(  );
		warehouseItems = new LinkedList<>(  );
		repositoryCommands = new LinkedList<>(  );

		addBehaviour( listenMessage( ) );
		addBehaviour( updateInfos( ) );
	}

	protected Behaviour listenMessage( ) {
		return new CyclicBehaviour( ) {
			@Override
			public void action ( ) {
				final ACLMessage message = receive( MessageTemplate.MatchInReplyTo( null ) );

				if ( message != null ) {
					final String content = receive( MessageTemplate.MatchInReplyTo( null ) ).getContent( );
					String struct1 = split( getValue( content ) ).get( 0 );
					String struct2 = split( getValue( content ) ).get( 1 );

					if ( content.startsWith( "error" ) ) {
						dispatcher.askFor( MANAGE_ERROR, struct1, struct2 );
					} else if ( content.startsWith( "confirmation" ) ) {
						ACLMessage reply = message.createReply( );

						String orderId = struct1.startsWith( "order_id" ) ? getValue( struct1 ) : getValue( struct2 );
						String orderInfo = struct1.startsWith( "info" ) ? getValue( struct1 ) : getValue( struct2 );

						dispatcher.<CompletableFuture<Boolean>>askFor( CONFIRMATION, orderId, orderInfo )
								.thenAccept( ack -> {
									reply.setContent( ( ack ? "confirm( " : "abort( " ) + orderId + ", " + orderInfo );
									send( reply );
								} );
					} else {
						System.out.println( "Received message: " + message.getContent( ) );     // TODO remove
						messages.add( message.getContent( ) );
					}
				} else block( );
			}
		};
	}

	protected Behaviour updateInfos( ) {
		return new CyclicBehaviour( ) {
			@Override
			public void action ( ) {
				if ( new Date( ).getTime( ) - lastUpdate > UPDATE_TIME ) {
					updateItems( );                                                 // update warehouse (items) info
					updateCommands( );                                              // update repository (commands) info
					lastUpdate = new Date( ).getTime( );
				}
				block( UPDATE_TIME );                                               // wait specified time
			}
		};
	}

	private void updateItems( ) {
		// setup the message and send it
		Structure info = new Structure( "info" );
		info.addTerm( new Structure( "warehouse" ) );
		sendCFP( ServiceType.MANAGEMENT_ITEMS.getName( ), ServiceType.INFO_WAREHOUSE.getName( ),
				buildACL( ACLMessage.CFP, info ), false ).thenAccept( message -> {
					synchronized ( this ) {
						warehouseItems = split( message.getContent( ) ).stream( ).map( Item::parse )
								.collect( Collectors.toList( ) );                   // update warehouse infos
					}
				} );
	}

	private void updateCommands( ) {
		// setup the message and send it
		Structure info = new Structure( "info" );
		info.addTerm( new Structure( "commands" ) );
		sendCFP( ServiceType.MANAGEMENT_COMMANDS.getName( ), ServiceType.INFO_COMMANDS.getName( ),
				buildACL( ACLMessage.CFP, info ), false ).thenAccept( message -> {
					synchronized ( this ) {
						repositoryCommands = split( message.getContent( ) ).stream( ).map( Command::parse )
								.collect( Collectors.toList( ) );                   // update repository info
					}
				} );
	}

	@Override
	public <T> T askFor ( Request request, String... args ) {
		switch ( request ) {
			case INFO_ITEMS_LIST:
			case INFO_WAREHOUSE_STATE:
				return ( T ) completableResultOf( ( ) -> warehouseItems, true );

			case INFO_COMMANDS:
				return ( T ) completableResultOf( ( ) -> repositoryCommands, true );

			case EXEC_COMMAND:
				Literal info = buildLiteral( "request", new SimpleStructure[] {
						new SimpleStructure( "command_id", args[ 0 ] ),
						new SimpleStructure( "version_id", args[ 1 ] )
				}, new Literal[] {} );

				sendMSG( ServiceType.MANAGEMENT_COMMANDS.getName( ), ServiceType.REQUEST_COMMAND.getName( ),
						buildACL( ACLMessage.REQUEST, info ), false );
				return null;

			case END:
				takeDown( );                                                        // terminate
				return null;

			case ORDER:
				placeOrder( args );
				return null;

			default:
				throw new NotImplementedException( NOT_IMPLEMENTED_REQUEST_MSG );
		}
	}

	private <T> CompletableFuture<T> completableResultOf( Supplier<T> resultSupplier, boolean synchronizedOnThis ) {
		CompletableFuture<T> result = new CompletableFuture<>( );

		addBehaviour( new OneShotBehaviour( ) {
			@Override
			public void action ( ) {
				if ( synchronizedOnThis ) {
					synchronized ( this ) {
						result.complete( resultSupplier.get( ) );
					}
				} else result.complete( resultSupplier.get( ) );
			}
		} );
		return result;
	}

	private void placeOrder( String... args ) {
		List<String> l = new ArrayList<>( Arrays.asList( args ) );

		Literal order = buildLiteral( "order", new SimpleStructure[] {
					new SimpleStructure( "client", l.remove( 0 ) ),
					new SimpleStructure( "address", l.remove( 0 ) )
				},
				( l.stream( ).collect( Collectors.groupingBy( e -> e, Collectors.counting( ) ) )
				.entrySet( ).stream( ).map( e -> buildLiteral( "item", new SimpleStructure[] {
						new SimpleStructure( "id", e.getKey( ) ),
						new SimpleStructure( "quantity", "" + e.getValue( ) )
				}, new Literal[]{} ) ).collect( Collectors.toList( ) ) ).toArray( new Literal[]{} )
		);

		sendMSG( ServiceType.MANAGEMENT_ORDERS.toString( ), ServiceType.ACCEPT_ORDER.toString( ),
				buildACL( ACLMessage.REQUEST, order ), false );
	}

	private ACLMessage buildACL( int performative, Serializable content ) {
		ACLMessage acl = new ACLMessage( performative );          // create a "call for propose" message

		try {
			if ( content instanceof String ) acl.setContent( ( String ) content );
			else acl.setContentObject( content );
		} catch ( IOException e ) {
			e.printStackTrace( );
		}

		return acl;
	}

	private void sendMSG( String serviceName, String serviceType, ACLMessage message, boolean toAll ) {
		DFAgentDescription template = new DFAgentDescription( );            // create a "service provider" template
		ServiceDescription sd = new ServiceDescription( );
		sd.setName( serviceName );
		sd.setType( serviceType );
		template.addServices( sd );

		Executors.newCachedThreadPool( ).submit( ( ) -> {
			try {
				DFAgentDescription[] result = DFService.search( this, template ); // an array containing all the agents that matches the template

				if ( result.length == 0 ) return;

				for ( int i = 0; ( i == 0 || toAll ) && i < result.length; i++ )
					message.addReceiver( result[i].getName( ) );                // add message's receiver

				send( message );                                                // send the cfp to all ability sellers
			} catch ( FIPAException e ) {
				e.printStackTrace( );
			}
		} );
	}

	// action of send a message
	private CompletableFuture<ACLMessage> sendCFP( String serviceName, String serviceType, ACLMessage message, boolean askAll ) {
		CompletableFuture<ACLMessage> returnValue = new CompletableFuture<>( );

		String msgId = java.time.LocalDateTime.now( ).toString( ) + Math.random( );

		message.setPerformative( ACLMessage.CFP );
		message.setReplyWith( msgId );

		sendMSG( serviceName, serviceType, message, askAll );

		Executors.newCachedThreadPool( ).submit( ( ) -> {
			ACLMessage s = blockingReceive( MessageTemplate.MatchInReplyTo( msgId ), RESPONSE_TIME );

			returnValue.complete( s );
		} );

		return returnValue;
	}
}
