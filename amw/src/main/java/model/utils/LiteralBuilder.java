package model.utils;

import jason.asSyntax.*;

import java.util.*;

public class LiteralBuilder {

	private String name;
	private List<Literal> values;
	private List<Literal> queue;

	public LiteralBuilder setName( String s ) {
		this.name = s;
		return this;
	}

	public LiteralBuilder addValues( String... l ) {
		if ( values == null ) values = new LinkedList<>( );
		for ( String s : l ) values.add( new Atom( s ) );
		return this;
	}

	public LiteralBuilder addValues( Literal... literals ) {
		if ( values == null ) values = new LinkedList<>( Arrays.asList( literals ) );
		else values.addAll( Arrays.asList( literals ) );
		return this;
	}

	public LiteralBuilder addQueue( String... l ) {
		if ( queue == null ) queue = new LinkedList<>( );
		for ( String s : l ) queue.add( new Atom( s ) );
		return this;
	}

	public LiteralBuilder addQueue( Literal... literals ) {
		if ( queue == null ) queue = new LinkedList<>( Arrays.asList( literals ) );
		else queue.addAll( Arrays.asList( literals ) );
		return this;
	}

	public Literal build( ) throws IllegalStateException {
		Literal literal = new LiteralImpl( new Atom( name ) );

		if ( values != null && ! values.isEmpty( ) ) literal.addTerms( values.toArray( new Literal[] {} ) );
		if ( queue != null && ! queue.isEmpty( ) ) literal.addAnnots( queue.toArray( new Literal[] {} ) );

		return literal;
	}

	public static Literal buildLiteral( String name, String value ) {
		return buildLiteral( name, value, new Literal[] { } );
	}

	public static Literal buildLiteral( String name, String value, Literal... list ) {
		Literal literal = new LiteralImpl( new Atom( name ) );
		literal.addTerm( new Atom( value ) );

		literal.addAnnots( list );

		return literal;
	}

	public static Literal buildLiteral( String name, SimpleStructure... structures ) {
		return buildLiteral( name, structures, new Literal[] {} );
	}

	public static Literal buildLiteral( String name, SimpleStructure[] structures, Literal... list ) {
		Literal literal = new LiteralImpl( new Atom( name ) );

		Arrays.asList( structures ).forEach( entry -> {
			Structure struct = new Structure( entry.getName( ) );
			struct.addTerm( entry.getValue( ) );
			literal.addTerm( struct );
		} );

		literal.addAnnots( list );

		return literal;
	}

	public static class SimpleStructure {

		private String name;
		private Term value;

		public SimpleStructure( String name, Atom value ) {
			this.name = name;
			this.value = value;
		}

		public SimpleStructure( String name, String value ) {
			this.name = name;
			this.value = new Atom( value );
		}

		public SimpleStructure( String name, Integer value ) {
			this.name = name;
			this.value = new NumberTermImpl( value );
		}

		public String getName ( ) {
			return name;
		}

		public Term getValue ( ) {
			return value;
		}
	}
}
