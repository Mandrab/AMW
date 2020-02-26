package model.utils;

import static model.utils.LiteralParser.*;
import static org.junit.Assert.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestLiteralUtils {

	@Test
	@SuppressWarnings( "unchecked" )
	public void testSplit( ) {
		List<Pair<String, String[]>> inOut = Arrays.asList( new ImmutablePair[]{
				new ImmutablePair<>( "", new String[]{ "" } ),
				new ImmutablePair<>( "str1(ciao)", new String[]{ "str1(ciao)" } ),
				new ImmutablePair<>( "str1(str2(ciao2), str3(ciao3))",
						new String[]{ "str1(str2(ciao2), str3(ciao3))" } ),
				new ImmutablePair<>( "str1(ciao1), str2(ciao2)", new String[]{ "str1(ciao1)", "str2(ciao2)" } ),
				new ImmutablePair<>( "str1(ciao1), str2(ciao2), str3(ciao3)",
						new String[]{ "str1(ciao1)", "str2(ciao2)", "str3(ciao3)" } ),
				new ImmutablePair<>( "[]", new String[]{ "" } ),
				new ImmutablePair<>( "[str1(ciao)]", new String[]{ "str1(ciao)" } ),
				new ImmutablePair<>( "[str1(str2(ciao2), str3(ciao3))]",
						new String[]{ "str1(str2(ciao2), str3(ciao3))" } ),
				new ImmutablePair<>( "[str1(ciao1), str2(ciao2)]", new String[]{ "str1(ciao1)", "str2(ciao2)" } ),
				new ImmutablePair<>( "[str1(ciao1), str2(ciao2), str3(ciao3)]",
						new String[]{ "str1(ciao1)", "str2(ciao2)", "str3(ciao3)" } ),
				new ImmutablePair<>( "[lit(1)[lit(2),lit(3)], lit(2)[lit(3),lit(4)]]",
						new String[]{ "lit(1)[lit(2),lit(3)]", "lit(2)[lit(3),lit(4)]" } )
		} );

		inOut.forEach( couple -> assertArrayEquals( couple.getValue( ),
				split( couple.getKey( ) ).toArray( new String[]{ "" } ) ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void testSplitStructAndList( ) {
		List<Pair<String, Pair<String, String>>> inOut = Arrays.asList( new ImmutablePair[]{
				new ImmutablePair<>( "", new ImmutablePair<>( "", "" ) ),
				new ImmutablePair<>( "str1(ciao)", new ImmutablePair<>( "str1(ciao)", "" ) ),
				new ImmutablePair<>( "str1(ciao)[]", new ImmutablePair<>( "str1(ciao)", "" ) ),
				new ImmutablePair<>( "str1(ciao)[str2(ciao2)]", new ImmutablePair<>( "str1(ciao)", "str2(ciao2)" ) ),
				new ImmutablePair<>( "str1(ciao)[str2(ciao2), str3(ciao3)]", new ImmutablePair<>( "str1(ciao)", "str2(ciao2), str3(ciao3)" ) ),
				new ImmutablePair<>( "str1(str2(ciao2)[str3(ciao3)])", new ImmutablePair<>( "str1(str2(ciao2)[str3(ciao3)])", "" ) ),
				new ImmutablePair<>( "str1(str2(ciao2)[str3(ciao3)[str4(ciao4)]])", new ImmutablePair<>( "str1(str2(ciao2)[str3(ciao3)[str4(ciao4)]])", "" ) ),
		} );

		inOut.forEach( couple -> assertEquals( couple.getValue( ), splitStructAndList( couple.getKey( ) ) ) );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void testGetValue( ) {
		List<Pair<String, String>> inOut = Arrays.asList( new ImmutablePair[]{
				new ImmutablePair<>( "str1(ciao)", "ciao" ),
				new ImmutablePair<>( "str1(ciao)[]", "ciao" ),
				new ImmutablePair<>( "str1(ciao)[str2(ciao2)]", "ciao" ),
				new ImmutablePair<>( "str1(str2(ciao2))", "str2(ciao2)" ),
				new ImmutablePair<>( "str1(str2(ciao2), str3(ciao3))", "str2(ciao2), str3(ciao3)" )
		} );

		try {
			getValue( "" );
		} catch ( RuntimeException ignored ) { }
		catch ( Exception e ) {
			fail( "should have thrown a NoValueException" );
		}

		try {
			getValue( "(" );
		} catch ( IllegalStateException ignored ) { }
		catch ( Exception e ) {
			fail( "should have thrown a IllegalStateException" );
		}

		try {
			getValue( "[(" );
		} catch ( IllegalStateException ignored ) { }
		catch ( Exception e ) {
			fail( "should have thrown a IllegalStateException" );
		}

		inOut.forEach( couple -> {
			try {
				assertEquals( couple.getValue( ), getValue( couple.getKey( ) ) );
			} catch ( Exception e ) {
				fail( "shouldn't throw Exception" );
			}
		} );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	public void testGetValueOf( ) {
		List<Triple<String, String, String>> inOut = Arrays.asList( new ImmutableTriple[]{
				new ImmutableTriple<>( "str1(ciao)", "str1", "ciao" ),
				new ImmutableTriple<>( "str1(ciao)[]", "str1", "ciao" ),
				new ImmutableTriple<>( "str1(ciao)[str2(ciao2)]", "str1", "ciao" ),
				new ImmutableTriple<>( "str1(str2(ciao2))", "str1", "str2(ciao2)" ),
				new ImmutableTriple<>( "str1(str2(ciao2), str3(ciao3))", "str1", "str2(ciao2), str3(ciao3)" ),
				new ImmutableTriple<>( "str1(ciao)[str2(ciao2)]", "str2", "ciao2" ),
				new ImmutableTriple<>( "str1(ciao)[str2(ciao2), str3(ciao3)]", "str3", "ciao3" ),
				new ImmutableTriple<>( "str1(str2(ciao2))", "str2", "ciao2" ),
				new ImmutableTriple<>( "str1(str2(ciao2), str3(ciao3))", "str3", "ciao3" ),
				new ImmutableTriple<>( "[str1(ciao), str2(ciao2), str3(ciao3)]", "str2", "ciao2" ),
		} );

		inOut.forEach( triple -> assertEquals( triple.getRight( ), getValue( triple.getLeft( ), triple.getMiddle( ) ) ) );
	}
}
