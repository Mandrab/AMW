package model.utils;

import jason.asSyntax.*;

import java.util.*;

public class LiteralBuilder {

	private String name;
	private ListTerm values;
	private ListTerm queue;

	public LiteralBuilder( ) { }

	public LiteralBuilder( String s ) {
		this.name = s;
	}

	public LiteralBuilder setName( String s ) {
		this.name = s;
		return this;
	}

	public LiteralBuilder addValues( String... l ) {
		if ( values == null ) values = new ListTermImpl( );                         // first add
		for ( String s : l ) values.add( new Atom( s ) );                           // add all string
		return this;
	}

	public LiteralBuilder addValues( Term... terms ) {
		if ( values == null ) values = new ListTermImpl( );
		values.addAll( Arrays.asList( terms ) );
		return this;
	}

	public LiteralBuilder addQueue( String... l ) {
		if ( queue == null ) queue = new ListTermImpl( );
		for ( String s : l ) queue.add( new Atom( s ) );
		return this;
	}

	public LiteralBuilder addQueue( Term... terms ) {
		if ( queue == null ) queue = new ListTermImpl( );
		queue.addAll( Arrays.asList( terms ) );
		return this;
	}

	public Literal build( ) throws IllegalStateException {
		Literal literal = new LiteralImpl( new Atom( name ) );

		if ( values != null ) literal.addTerms( values.toArray( new Term[] {} ) );
		if ( queue != null ) literal.addAnnots( queue.toArray( new Term[] {} ) );

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
