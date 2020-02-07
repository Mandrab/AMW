package model.agent;

import interpackage.Command;
import interpackage.Item;
import interpackage.RequestDispatcher;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static interpackage.RequestHandler.Request.*;
import static model.utils.LiteralUtils.*;

public class ClientAgentImpl extends Agent implements ClientAgent {

	private RequestDispatcher dispatcher;

	@Override
	protected void setup( ) {
		dispatcher = ( RequestDispatcher ) getArguments( )[ 0 ];
		dispatcher.register( this );
	}

	@Override
	public <T> T askFor ( Request request, String... args ) {
		switch ( request ) {
			case INFO_ITEMS_LIST:
			case INFO_WAREHOUSE_STATE:
				return ( T ) getItemsInfo( request == INFO_WAREHOUSE_STATE );

			case INFO_COMMANDS:
				return ( T ) getCommandsInfo( );

			case EXEC_COMMAND:
				execCommand( args[ 0 ], args[ 1 ] );
				break;

			case END:
				takeDown( );
				break;

			case ORDER:
				placeOrder( args );
				break;

			default:
				throw new NotImplementedException( "The return of the required element has not yet been implemented. Sorry for the discomfort" );
		}
		return null;
	}

	private void execCommand( String scriptId, String versionId ) {
		// setup the message and send it
		Structure info = new Structure( "request" );
		Structure cId = new Structure( "command_id" );
		cId.addTerm( new Structure( scriptId ) );
		Structure vId = new Structure( "version_id" );
		vId.addTerm( new Structure( versionId ) );
		info.addTerms( cId, vId );

		sendCFP( ServiceType.MANAGEMENT_COMMANDS.getName( ), ServiceType.REQUEST_COMMAND.getName( ), info, false );
	}

	private CompletableFuture<List<Command>> getCommandsInfo( ) {
		// setup the message and send it
		Structure info = new Structure( "info" );
		info.addTerm( new Structure( "commands" ) );
		CompletableFuture<ACLMessage> response = sendCFP( ServiceType.MANAGEMENT_COMMANDS.getName( ), ServiceType.INFO_COMMANDS.getName( ), info, false );

		// create the return obj
		CompletableFuture<List<Command>> result = new CompletableFuture<>( );

		// when a response came, complete the result in an appropriate way
		response.thenAccept( aclMessage -> result.complete( parseCommands( aclMessage.getContent( ) ) ) );

		// return the value
		return result;
	}

	private CompletableFuture<List<?>> getItemsInfo( boolean includePositions ) {
		// setup the message and send it
		Structure info = new Structure( "info" );
		info.addTerm( new Structure( "warehouse" ) );
		CompletableFuture<ACLMessage> response = sendCFP( ServiceType.MANAGEMENT_ITEMS.getName( ), ServiceType.INFO_WAREHOUSE.getName( ), info, false );

		// create the return obj
		CompletableFuture<List<?>> result = new CompletableFuture<>( );

		// when a response came, complete the result
		response.thenAccept( aclMessage -> result.complete( parseItems( aclMessage.getContent( ) ) ) );

		// return the value
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

		sendCFP( ServiceType.MANAGEMENT_ORDERS.toString( ), ServiceType.ACCEPT_ORDER.toString( ), order, false ).thenAccept( response -> {
			if ( response.getContent( ).startsWith( "error" ) ) {
				System.out.println( "error" );
			} else if ( response.getContent( ).startsWith( "confirmation" ) ) {
				ACLMessage reply = response.createReply( );

				Pattern pattern = Pattern.compile( "order_id([^,]*)" );
				Matcher matcher = pattern.matcher( response.getContent( ) );
				String orderId = matcher.find( ) ? matcher.group( 1 ) : "";
				orderId = orderId.substring( 1, orderId.length( ) -1 );

				pattern = Pattern.compile( "info(.*)" );
				matcher = pattern.matcher( response.getContent( ) );
				String orderInfo = matcher.find( ) ? matcher.group( 1 ) : "";
				orderInfo = orderInfo.substring( 1, orderInfo.length( ) -1 );

				if ( dispatcher.askFor( CONFIRMATION ) )
					reply.setContent( "confirm( " + orderId + ", " + orderInfo );
				else
					reply.setContent( "abort( " + orderId + ", " + orderInfo );

				send( reply );
			}
		} );
	}

	// action of send a message TODO -> makeOrder
	private CompletableFuture<ACLMessage> sendCFP( String serviceName, String serviceType, Serializable input, boolean askAll ) {
		DFAgentDescription template = new DFAgentDescription( );            // create a "service provider" template
		ServiceDescription sd = new ServiceDescription( );
		sd.setName( serviceName );
		sd.setType( serviceType );
		template.addServices( sd );

		CompletableFuture<ACLMessage> returnValue = new CompletableFuture<>( );

		Executors.newCachedThreadPool().submit( ( ) -> {
			try {
				DFAgentDescription[] result = DFService.search(this, template); // an array containing all the agents that matches the template

				if ( result.length == 0 ) return;

				ACLMessage cfp = new ACLMessage( ACLMessage.CFP );          // create a "call for propose" message
				for ( int i = 0; ( i == 0 || askAll ) && i < result.length; i++ )
					cfp.addReceiver( result[i].getName( ) );                // add message's receiver

				// set the content
				if ( input instanceof String ) cfp.setContent( ( String ) input );
				else cfp.setContentObject( input );

				String msgId = java.time.LocalDateTime.now( ).toString( ) + Math.random( );
				cfp.setReplyWith( msgId );

				send( cfp );                                                // send the cfp to all ability sellers

				ACLMessage s = blockingReceive( MessageTemplate.MatchInReplyTo( msgId ), 50000 );

				returnValue.complete( s );

			} catch ( FIPAException | IOException e ) {
				e.printStackTrace( );
			}
		} );

		return returnValue;
	}

	private List<Item> parseItems ( String content ) {
		return split( content ).stream( ).map( Item::parse ).collect( Collectors.toList( ) );
	}

	private List<Command> parseCommands ( String content ) {
		return split( content ).stream( ).map( Command::parse ).collect( Collectors.toList( ) );
	}
}
