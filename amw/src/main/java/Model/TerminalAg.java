package Model;

import Controller.Mediator;
import InterpackageDatas.Item;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jason.asSyntax.*;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static Controller.Mediator.CommandOntology.*;
import static Controller.Mediator.RequireOntology.WAREHOUSE_STATE;
import static Model.utils.ServiceType.*;

public class TerminalAg extends Agent implements ModelAgent {

	private Mediator mediator;

	// start the jade agent
	public static void startAg( Mediator mediator ) {
		try {

			Runtime rt = Runtime.instance(  );                                      // get a hold on JADE runtime

			Profile p = new ProfileImpl(  );                                        // create a default profile

			ContainerController cc = rt.createAgentContainer( p );                  // create a new non-main container

			AgentController agent =                                                 // Create a new agent
					cc.createNewAgent( "terminal-ag",
							TerminalAg.class.getCanonicalName( ),
							new Object[]{ mediator } );

			agent.start(  );                                                        // fire up the agent

		} catch ( StaleProxyException e ) {
			System.err.println( "TerminalAgent: exception in startAg" );
			e.printStackTrace( );
		}
	};

	@Override
	protected void setup() {
		this.mediator = ( Mediator ) getArguments()[ 0 ];
		mediator.setAgent( this );
	}

	// action of send a message TODO -> makeOrder
	private CompletableFuture<ACLMessage> sendAction( String serviceName, String serviceType, Serializable input ) {
		DFAgentDescription template = new DFAgentDescription();                     // create a "service provider" template
		ServiceDescription sd = new ServiceDescription();
		if ( ! serviceName.isEmpty( ) )
			sd.setName( serviceName );
		if ( ! serviceType.isEmpty( ) )
			sd.setType( serviceType );
		template.addServices( sd );

		CompletableFuture<ACLMessage> returnValue = new CompletableFuture<>( );

		Executors.newCachedThreadPool().submit( ( ) -> {
			DFAgentDescription[] result = new DFAgentDescription[ 0 ];         // an array containing all the agents that matches the template
			try {
				result = DFService.search(this, template);

				if ( result.length == 0 ) {
					mediator.commandView( ERROR, "Info", "Error, no device able to handle the command!" );
					return;
				}

				ACLMessage cfp = new ACLMessage( ACLMessage.CFP );                        // create a "call for propose" message
				for ( int i = 0; i < result.length; i++ )
					cfp.addReceiver( result[i].getName(  ) );                             // add message's receiver

				if ( input instanceof String )
					cfp.setContent( ( String ) input );
				else
					cfp.setContentObject( input );

				send( cfp );                                                          // send the cfp to all ability sellers

				returnValue.complete( blockingReceive( 5000 ) );

			} catch ( FIPAException | IOException e ) {
				e.printStackTrace( );
			}
		});
		return returnValue;
	}

	@Override
	public void command ( Mediator.CommandOntology c, String... args ) {
		if ( c == END ) {
			takeDown(  );
		} else if ( c == SEND ) {
			List<String> l = new ArrayList<>( Arrays.asList( args ) );

			Literal order = new LiteralImpl( new Atom( "order" ) );
			Structure client = new Structure( "client" );
			client.addTerm( new StringTermImpl( l.remove( 0 ) ) );
			Structure address = new Structure( "address" );
			address.addTerm( new StringTermImpl( l.remove( 0 ) ) );
			order.addTerms( client, address );

			order.addAnnots( l.stream( ).map( s -> new StringTermImpl( s ) ).collect( Collectors.toList( ) ) );
			CompletableFuture<ACLMessage> msg = sendAction( MANAGEMENT_ORDERS.toString( ), ACCEPT_ORDER.toString( ), order );
			msg.thenAccept( m -> mediator.commandView( INPUT, m.getSender( ).getName( ).replaceAll( "@(\n|.)*", "" ), m.getContent(  ) ) );
		}
	}

	@Override @SuppressWarnings( "unchecked" )
	public <T> T ask ( Mediator.RequireOntology c ) {

		if ( c == WAREHOUSE_STATE ) {

			// setup the message and send it
			Structure info = new Structure( "info" );
			info.addTerm( new Structure( "warehouse" ) );
			CompletableFuture<ACLMessage> response = sendAction( MANAGEMENT_ITEMS.getName( ),
					INFO_WAREHOUSE.getName( ), info );

			// create the return obj
			CompletableFuture<List<Item>> result = new CompletableFuture<>( );

			// when a response came, complete the result
			response.thenAccept( aclMessage -> result.complete( parseItems( aclMessage.getContent( ) ) ) );

			// return the value
			try {
				return ( T ) result;
			} catch( ClassCastException e ) {
				throw new IllegalStateException( "Return and requested item types are different!" );
			}
		}

		throw new NotImplementedException( "The return of the required element has not yet been implemented. Sorry for the discomfort" );
	}

	private List<Item> parseItems ( String content ) {
		List<String> l = new ArrayList( Arrays.asList( content.substring( 1, content.length( ) -1 ) // remove initial and final []
			.split( "," ) ) );

		for( int i = 0; i < l.size( ); ) {
			if ( l.get( i ).contains( "[" ) && ! l.get( i ).endsWith( "]" ) ) {
				l.set( i, l.get( i ) + ',' + l.get( i +1 ) );
				l.remove( i + 1 );
			} else i++;
		}

		Pattern ITEMID_PATTERN = Pattern.compile("item\\(\"?([A-Z]|[a-z]|[0-9]| )*\"?\\)");
		Pattern RACK_PATTERN = Pattern.compile("rack\\([0-9]+\\)");
		Pattern SHELF_PATTERN = Pattern.compile("shelf\\([0-9]+\\)");
		Pattern QUANTITY_PATTERN = Pattern.compile("quantity\\([0-9]+\\)");

		return l.stream( ).map( s -> {
			Matcher matcher = ITEMID_PATTERN.matcher( s );
			String itemId = matcher.find( )
					? matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
							matcher.group( ).length( ) -1 )	: "Error";
			matcher = RACK_PATTERN.matcher( s );
			int rackId = matcher.find( )
					? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
							matcher.group( ).length( ) -1 ) ) : -1;
			matcher = SHELF_PATTERN.matcher( s );
			int shelfId = matcher.find( )
					? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
							matcher.group( ).length( ) -1 ) ) : -1;
			matcher = QUANTITY_PATTERN.matcher( s );
			int quantity = matcher.find( )
					? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
							matcher.group( ).length( ) -1 ) ) : -1;
			return new Item( itemId, rackId, shelfId, quantity );
		} ).collect( Collectors.toList( ) );
	}
}
