package model.utils;

import jason.NoValueException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LiteralUtils {

	public static String getValue( String structure, String valueOf ) {
		Pair<String, String> parts = splitStructAndList( structure );
		List<String> structures = split( parts.getKey( ) );
		List<String> lists = split( parts.getValue( ) );

		for ( String struct : structures ) {                        // may exist more than a struct
			if ( struct.startsWith( valueOf ) )                     // check if i want the value of the structure itself
				return getValue( struct );
			else {
				try {
					String s = getValue( getValue( struct ), valueOf );
					if ( s != null ) return s;
				} catch( RuntimeException ignored ) { }
			}
		}

		for ( String struct : lists ) {                             // may exist in the list
			if ( struct.startsWith( valueOf ) )                     // check if i want the value of the structure itself
				return getValue( struct );
			else {
				try {
					String s = getValue( getValue( struct ), valueOf );
					if ( s != null ) return s;
				} catch( RuntimeException ignored ) { }
			}
		}

		return null;
	}

	public static String getValue( String structure ) throws RuntimeException {
		if ( ! structure.contains( "(" ) )
			throw new RuntimeException( "the passed structure has no parenthesis value" );
		if ( ! structure.contains( ")" ) )
			throw new IllegalStateException( "no ')' is present" );
		if ( structure.startsWith( "[" ) )
			throw new IllegalStateException( "is possible to retrieve only the value of a structure or a literal" );

		return structure.endsWith( "]" )
				? splitStructAndList( structure ).getKey( )
						.substring( structure.indexOf( "(" ) + 1, structure.indexOf( "[" ) - 1 )
				: structure.substring( structure.indexOf( "(" ) + 1, structure.length( ) - 1 );
	}

	public static Pair<String, String> splitStructAndList ( String literal ) {
		if ( literal.startsWith( "[" ) ) {                              // a simple list
			if ( literal.endsWith( "]" ) ) return new ImmutablePair<>( "", literal );
			else throw new IllegalStateException( );                    // wrong formatted
		}

		if ( ! literal.endsWith( "]" ) )
			return new ImmutablePair<>( literal, "" );

		Vector<Character> chars = literal.chars( ).mapToObj( i -> ( char ) i )
				.collect( Collectors.toCollection( Vector::new ) );
		int startingIndex = 0;
		int openedParenthesis = 0;

		for ( int i = 0; i < chars.size( ); i ++ ) {
			if ( chars.get( i ).equals( '[' ) && openedParenthesis++ == 0 )
				startingIndex = i;
			else if ( chars.get( i ).equals( ']' ) )
				openedParenthesis--;
		}

		return new ImmutablePair<>( literal.substring( 0, startingIndex ),
				literal.substring( startingIndex +1, literal.length( ) -1 ) );
	}

	public static List<String> split( String list ) {
		if ( list.startsWith( "[" ) && list.endsWith( "]" ) )
			list = list.substring( 1, list.length( ) -1 );
		Vector<Character> chars = list.chars( ).mapToObj( i -> ( char ) i )
				.collect( Collectors.toCollection( Vector::new ) );
		List<String> output = new LinkedList<>(  );
		int startingIndex = 0;
		int openedParenthesis = 0;


		for ( int i = 0; i < chars.size( ); i ++ ) {
			if ( chars.get( i ).equals( ',' ) && openedParenthesis == 0 ) {
				final StringBuilder sb = new StringBuilder( i - startingIndex );
				IntStream.range( startingIndex, i ).forEach( idx -> sb.append( chars.get( idx ) ) );
				output.add( sb.toString( ).replaceAll( " ", "" ) );
				startingIndex = i + 1;
			} else if ( chars.get( i ).equals( '(' ) )
				openedParenthesis++;
			else if ( chars.get( i ).equals( ')' ) )
				openedParenthesis--;
		}

		if ( ! output.isEmpty( ) && list.contains( "," ) )
			output.add( list.substring( list.lastIndexOf( "," ) + 1 ).replaceAll( " ", "" ) );
		else
			output.add( list.replaceAll( " ", "" ) );

		return output;
	}
}
