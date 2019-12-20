package Model;

import Controller.AgentMediator;
import Controller.Mediator;
import InterpackageDatas.Item;
import asl_actions.order_specs;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static Controller.Mediator.CommandOntology.*;
import static Controller.Mediator.RequireOntology.WAREHOUSE_STATE;
import static Model.utils.ServiceType.*;

public class InterfaceAgentImpl extends Agent implements InterfaceAgent {

	private static final Logger logger = Logger.getLogger( order_specs.class.getName( ) );

	// start the jade agent
	public static void startAg( Mediator mediator ) {
		try {

			Runtime rt = Runtime.instance(  );                                      // get a hold on JADE runtime

			Profile p = new ProfileImpl(  );                                        // create a default profile

			ContainerController cc = rt.createAgentContainer( p );                  // create a new non-main container

			AgentController agent =                                                 // Create a new agent
					cc.createNewAgent( "interface-ag",
							InterfaceAgentImpl.class.getCanonicalName( ),
							new Object[]{ mediator } );

			agent.start(  );                                                        // fire up the agent

		} catch ( StaleProxyException e ) {
			logger.info( "exception in startAg" );
			e.printStackTrace( );
		}
	};

	@Override
	protected void setup( ) {
		( ( AgentMediator ) getArguments()[ 0 ] ).setAgent( this );
	}

	// action of send a message TODO -> makeOrder
	public CompletableFuture<ACLMessage> sendCFP( String serviceName, String serviceType, Serializable input, boolean askAll ) {
		DFAgentDescription template = new DFAgentDescription();                     // create a "service provider" template
		ServiceDescription sd = new ServiceDescription();
		if ( ! serviceName.isEmpty( ) )
			sd.setName( serviceName );
		if ( ! serviceType.isEmpty( ) )
			sd.setType( serviceType );
		template.addServices( sd );

		CompletableFuture<ACLMessage> returnValue = new CompletableFuture<>( );

		Executors.newCachedThreadPool().submit( ( ) -> {
			try {
				DFAgentDescription[] result = DFService.search(this, template); // an array containing all the agents that matches the template

				if ( result.length == 0 ) return;

				ACLMessage cfp = new ACLMessage( ACLMessage.CFP );                        // create a "call for propose" message
				for ( int i = 0; ( i == 0 || askAll ) && i < result.length; i++ )
					cfp.addReceiver( result[i].getName( ) );                             // add message's receiver

				// set the content
				if ( input instanceof String ) cfp.setContent( ( String ) input );
				else cfp.setContentObject( input );

				send( cfp );                                                          // send the cfp to all ability sellers

				returnValue.complete( blockingReceive( 5000 ) );

			} catch ( FIPAException | IOException e ) {
				e.printStackTrace( );
			}
		} );
		return returnValue;
	}

	@Override
	public void command ( Mediator.CommandOntology c, String... args ) {
		if ( c == END ) {
			takeDown( );
		} else if ( c == SEND ) {
			List<String> l = new ArrayList<>( Arrays.asList( args ) );

			Literal order = new LiteralImpl( new Atom( "order" ) );
			Structure client = new Structure( "client" );
			client.addTerm( new StringTermImpl( l.remove( 0 ) ) );
			Structure address = new Structure( "address" );
			address.addTerm( new StringTermImpl( l.remove( 0 ) ) );
			order.addTerms( client, address );

			ListTerm terms = new ListTermImpl( );
			terms.addAll( l.stream( ).map( StringTermImpl::new ).collect( Collectors.toList( ) ) );
			order.addAnnot( terms );
			sendCFP( MANAGEMENT_ORDERS.toString( ), ACCEPT_ORDER.toString( ), order, false );
		}
	}

	@Override @SuppressWarnings( "unchecked" )
	public <T> T ask ( Mediator.RequireOntology c ) {

		if ( c == WAREHOUSE_STATE ) {

			// setup the message and send it
			Structure info = new Structure( "info" );
			info.addTerm( new Structure( "warehouse" ) );
			CompletableFuture<ACLMessage> response = sendCFP( MANAGEMENT_ITEMS.getName( ),
					INFO_WAREHOUSE.getName( ), info, false );

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
