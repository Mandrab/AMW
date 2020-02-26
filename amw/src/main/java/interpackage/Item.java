package interpackage;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static model.utils.LiteralParser.*;

/*
 * This class represent an item in a specific position in the warehouse
 */
public class Item {

	private String itemId;
	private int reserved;
	List<Triple<Integer, Integer, Integer>> positions;

	public Item ( String itemId, int reserved, List<Triple<Integer, Integer, Integer>> positions ) {
		this.itemId = itemId;
		this.reserved = reserved;
		this.positions = positions;
	}

	public String getItemId ( ) {
		return itemId;
	}

	public int getReserved ( ) {
		return reserved;
	}

	public List<Triple<Integer, Integer, Integer>> getPositions ( ) {
		return positions;
	}

	static public Item parse ( String input ) {
		String itemId = getValue( input, "id" );
		int reserved = Integer.parseInt( Objects.requireNonNull( getValue( input, "reserved" ) ) );
		return new Item( itemId, reserved, split( splitStructAndList( input ).getValue( ) ).stream( ).map( s ->
				new ImmutableTriple<>( Integer.parseInt( Objects.requireNonNull( getValue( input, "rack" ) ) ),
						Integer.parseInt( Objects.requireNonNull( getValue( input, "shelf" ) ) ),
						Integer.parseInt( Objects.requireNonNull( getValue( input, "quantity" ) ) ) ) )
				.collect( Collectors.toList( ) ) );
	}

	@Override
	public Item clone( ) {
		return new Item( itemId, reserved, positions.stream( ).map( t -> new ImmutableTriple<>( t.getLeft( ),
				t.getMiddle( ), t.getRight( ) ) ).collect( Collectors.toList( ) ) );
	}

	@Override
	public boolean equals( Object obj ) {
		if ( ! ( obj instanceof Item ) ) return false;

		Item itm = ( Item ) obj;
		return itemId.equals( itm.itemId ) && reserved == itm.reserved && positions.equals( itm.positions );
	}

	@Override
	public String toString( ) {
		return "itemId: " + itemId + ", reserved: " + reserved + positions.stream( ).map( t -> "\n\track: "
				+ t.getLeft( ) + ", shelf: " + t.getMiddle( ) + ", quantity: " + t.getRight( ) )
				.collect( Collectors.joining( ) ) + "\n";
	}
}
