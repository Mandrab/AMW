package Model.Agent;

import Interpackage.Item;
import Interpackage.RequestDispatcher;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jason.asSyntax.*;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Interpackage.RequestHandler.Request.CONFIRMATION;
import static Interpackage.RequestHandler.Request.INFO_WAREHOUSE_STATE;
import static Model.utils.ServiceType.*;
import static Model.utils.ServiceType.ACCEPT_ORDER;

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
			case INFO_WAREHOUSE_STATE:
			case INFO_ITEMS_LIST:
				return getItemsInfo( request == INFO_WAREHOUSE_STATE );

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

	private <T> T getItemsInfo( boolean includePositions ) {
		// setup the message and send it
		Structure info = new Structure( "info" );
		info.addTerm( new Structure( "warehouse" ) );
		CompletableFuture<ACLMessage> response = sendCFP( MANAGEMENT_ITEMS.getName( ), INFO_WAREHOUSE.getName( ), info, false );

		// create the return obj
		CompletableFuture<List<?>> result = new CompletableFuture<>( );

		// when a response came, complete the result in an appropriate way
		response.thenAccept( aclMessage -> {
			if ( includePositions )
				result.complete( parseItems( aclMessage.getContent( ) ) );
			else
				result.complete( parseItems( aclMessage.getContent( ) ).stream( ).flatMap( item -> IntStream.range( 0, item.getQuantity( ) ).mapToObj( i -> item.getItemId( ) ) ).collect( Collectors.toList( ) ) );
		} );

		// return the value
		try {
			return ( T ) result;
		} catch ( ClassCastException e ) {
			throw new IllegalStateException( "Return and requested item types are different!" );
		}
	}

	private void placeOrder( String... args ) {
		List<String> l = new ArrayList<>( Arrays.asList( args ) );

		Literal order = new LiteralImpl( new Atom( "order" ) );
		Structure client = new Structure( "client" );
		client.addTerm( new StringTermImpl( l.remove( 0 ) ) );
		Structure address = new Structure( "address" );
		address.addTerm( new StringTermImpl( l.remove( 0 ) ) );
		order.addTerms( client, address );

		ListTerm terms = new ListTermImpl( );
		terms.addAll( l.stream( ).map( Atom::new ).collect( Collectors.toList( ) ) );
		order.addAnnot( terms );
		sendCFP( MANAGEMENT_ORDERS.toString( ), ACCEPT_ORDER.toString( ), order, false ).thenAccept( response -> {
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

				send( cfp );                                                // send the cfp to all ability sellers

				returnValue.complete( blockingReceive( 50000 ) );

			} catch ( FIPAException | IOException e ) {
				e.printStackTrace( );
			}
		} );

		return returnValue;
	}

	private List<Item> parseItems ( String content ) {
		List<String> l = new ArrayList<>( Arrays.asList( content.substring( 1, content.length( ) -1 ) // remove initial and final []
				.split( "," ) ) );

		for( int i = 0; i < l.size( ); ) {
			if ( l.get( i ).contains( "[" ) && ! l.get( i ).endsWith( "]" ) ) {
				l.set( i, l.get( i ) + ',' + l.get( i +1 ) );
				l.remove( i + 1 );
			} else i++;
		}

		return l.stream( ).map( Item::parse ).collect( Collectors.toList( ) );
	}

}
