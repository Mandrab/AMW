package interpackage;

import java.util.Objects;

import static model.utils.LiteralUtils.getValue;

/*
 * This class represent an item in a specific position in the warehouse
 */
public class Item {

	private String itemId;
	private int rackId;
	private int shelfId;
	private int quantity;

	public Item ( String itemId, int rackId, int shelfId, int quantity ) {
		this.itemId = itemId;
		this.rackId = rackId;
		this.shelfId = shelfId;
		this.quantity = quantity;
	}

	public String getItemId ( ) {
		return itemId;
	}

	public int getRackId ( ) {
		return rackId;
	}

	public int getShelfId ( ) {
		return shelfId;
	}

	public int getQuantity ( ) {
		return quantity;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( ! ( obj instanceof Item ) ) return false;

		Item itm = ( Item ) obj;
		return itemId.equals( itm.itemId ) && rackId == itm.rackId && shelfId == itm.shelfId
				&& quantity == itm.quantity;
	}

	@Override
	public String toString( ) {
		return "itemId: " + itemId + ", rack: " + rackId + ", shelf: " + shelfId + ", quantity: " + quantity;
	}

	public int first( Item i ) {
		if ( rackId - i.rackId != 0 )
			return rackId - i.rackId;
		return shelfId - i.shelfId;
	}

	static public Item parse ( String input ) {
		String itemId = getValue( input, "id" );
		int rackId = Integer.parseInt( Objects.requireNonNull( getValue( input, "rack" ) ) );
		int shelfId = Integer.parseInt( Objects.requireNonNull( getValue( input, "shelf" ) ) );
		int quantity = Integer.parseInt( Objects.requireNonNull( getValue( input, "quantity" ) ) );
		return new Item( itemId, rackId, shelfId, quantity );
	}
}
