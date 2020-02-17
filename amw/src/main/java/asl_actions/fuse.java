package asl_actions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import model.utils.LiteralUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import static model.utils.LiteralUtils.*;

public class fuse extends DefaultInternalAction {

	private static final String ITEM_POS_MISSING = "The items has not been found in the positions list";

	@Override
	public Object execute( TransitionSystem ts, Unifier un, Term[] args) {

		List<String> items = split( args[ 0 ].toString( ) );
		List<String> positions = split( args[ 1 ].toString( ) );

		ListTerm result = new ListTermImpl( );
		result.addAll( items.stream( ).map( i -> LiteralUtils.buildLiteral( "item", new SimpleStructure[] {
				new SimpleStructure( "id", getValue( i, "id" ) ),
				new SimpleStructure( "quantity", getValue( i, "quantity" ) ),
		}, split( splitStructAndList( positions.stream( )
				.filter( p -> Objects.equals( getValue( i, "id" ), getValue( p, "id" ) ) )
				.findFirst( ).orElseThrow( ( ) -> new NoSuchElementException( ITEM_POS_MISSING ) ) ).getValue( ) ).stream( )
				.map( p -> LiteralUtils.buildLiteral( "position", new SimpleStructure( "rack", getValue( p, "rack" ) ),
						new SimpleStructure( "shelf", getValue( p, "shelf" ) ),
						new SimpleStructure( "quantity", getValue( p, "quantity" ) )
				) ).collect( Collectors.toList( ) ).toArray( new Literal[] {} ) ) )
				.collect( Collectors.toList( ) ) );
		un.unifies( result, args[ 2 ] );

		return true;
	}

}
