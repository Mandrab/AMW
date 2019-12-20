package asl_actions;

import java.util.logging.Logger;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.Map;

public class order_specs extends DefaultInternalAction {

	private static final Logger logger = Logger.getLogger( order_specs.class.getName( ) );

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) throws Exception {

		Literal literal = ( Literal )args[ 0 ];

		if ( ! literal.getFunctor( ).equals( "order" ) ) return false;
		String client = ( String )( ( Map ) ( ( ObjectTerm ) literal.getTerm( 0 ) ).getObject( ) ).get( "client" );
		un.unifies( args[ 1 ], new StringTermImpl( client ) );
		String address = ( String )( ( Map ) ( ( ObjectTerm ) literal.getTerm( 0 ) ).getObject( ) ).get( "address" );
		un.unifies( args[ 2 ], new StringTermImpl( address ) );

		StringBuilder stringBuilder = new StringBuilder( "Order incoming:\n"
				+ "\tClient: " + client + "\n"
				+ "\tAddress: " + address + "\n"
				+ "\tItems:" );

		literal.getAnnots( ).forEach( term -> stringBuilder.append( "\n\t\t" + term ) );
		un.unifies( args[ 3 ], literal.getAnnots(  ) );

		//logger.info( stringBuilder.toString( ) );

		return true;
	}

}
