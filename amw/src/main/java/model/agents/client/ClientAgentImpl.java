package model.agents.client;

import interpackage.Item;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import model.agents.TerminalAgentImpl;
import model.utils.ServiceType;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.*;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.or;
import static model.utils.LiteralParser.*;
import static model.utils.LiteralBuilder.*;

public class ClientAgentImpl extends TerminalAgentImpl {

	private static final String NOT_IMPLEMENTED_REQUEST_MSG = "The return of the required element has not yet been " +
			"implemented. Sorry for the discomfort";
	private static final int UPDATE_TIME = 1000;// TODO

	private List<Item> warehouseItems;
	private long lastUpdate;

	@Override
	protected void setup( ) {
		super.setup( );

		warehouseItems = new LinkedList<>(  );

		addBehaviour( listenMessage( ) );
		addBehaviour( updateItems( ) );
	}

	protected Behaviour listenMessage( ) {
		return new CyclicBehaviour( ) {
			@Override
			public void action ( ) {
				// TODO serve sta roba?
				final ACLMessage message = receive( or( MessageTemplate.MatchPerformative( ACLMessage.FAILURE ),
						MessageTemplate.MatchPerformative( ACLMessage.REQUEST ) ) );

				if ( message != null ) {
					final String content = message.getContent( );
					String struct1 = split( getValue( content ) ).get( 0 );
					String struct2 = split( getValue( content ) ).get( 1 );

					if ( message.getPerformative( ) == ACLMessage.CONFIRM &&
							content.startsWith( "confirmation" ) ) {
						dispatcher.askFor( ORDER_STATUS, getValue( struct1 ), getValue( struct2 ) );
					} else {
						System.out.println( "Received message: " + message.getContent( ) );     // TODO
					}
				} else block( );
			}
		};
	}

	protected Behaviour updateItems( ) {
		return new CyclicBehaviour( ) {
			@Override
			public void action ( ) {
				if ( new Date( ).getTime( ) - lastUpdate > UPDATE_TIME ) {              // update warehouse (items) info
					// setup the message and send it
					Structure info = new Structure( "info" );
					info.addTerm( new Structure( "warehouse" ) );
					new MessageSender( ServiceType.MANAGEMENT_ITEMS.getName( ), ServiceType.INFO_WAREHOUSE.getName( ),
							CFP, info ).require( getAgent( ) ).thenAccept( message -> {
						synchronized ( this ) {
							warehouseItems = split( message.getContent( ) ).stream( ).map( Item::parse )
									.collect( Collectors.groupingBy( Item::getItemId ) ).entrySet( ).stream( )
									.map( p -> new Item( p.getKey( ), p.getValue( ).get( 0 ).getReserved( ), p.getValue( )
											.stream( ).flatMap( i -> i.getPositions( ).stream( ) )
											.collect( Collectors.toList( ) ) ) )
									.collect( Collectors.toList( ) );                   // update warehouse infos
						}
					} );
					lastUpdate = new Date( ).getTime( );
				}
				block( UPDATE_TIME );                                                   // wait specified time
			}
		};
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T askFor( Request request, String... args ) {
		if ( request == INFO_ITEMS_LIST || request == INFO_WAREHOUSE_STATE ) {
			CompletableFuture<List<Item>> result = new CompletableFuture<>( );
			addBehaviour( new OneShotBehaviour( ) {
				@Override
				public void action ( ) {
					synchronized ( this ) {
						result.complete( warehouseItems.stream( ).map( Item::clone ).collect( Collectors.toList( ) ) );
					}
				}
			} );
			return ( T ) result;
		}

		if ( request == ORDER ) {
			placeOrder( args );
			return null;
		}

		if ( request == END ) {
			takeDown( );
			return null;
		}

		throw new NotImplementedException( NOT_IMPLEMENTED_REQUEST_MSG );
	}

	private void placeOrder( String... args ) {
		List<String> l = new ArrayList<>( Arrays.asList( args ) );

		Literal order = buildLiteral( "order", new SimpleStructure[] {
					new SimpleStructure( "client", l.remove( 0 ) ),
					new SimpleStructure( "email", l.remove( 0 ) ),
					new SimpleStructure( "address", l.remove( 0 ) )
				},
				( l.stream( ).collect( Collectors.groupingBy( e -> e, Collectors.counting( ) ) )
				.entrySet( ).stream( ).map( e -> buildLiteral( "item", new SimpleStructure( "id", e.getKey( ) ),
						new SimpleStructure( "quantity", "" + e.getValue( ) ) ) ).collect( Collectors.toList( ) ) )
						.toArray( new Literal[]{} )
		);

		new MessageSender( ServiceType.MANAGEMENT_ORDERS.toString( ), ServiceType.ACCEPT_ORDER.toString( ), REQUEST,
				order ).send( this );
	}
}
