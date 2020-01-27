package Interpackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public String toString( ) {
		return "itemId: " + itemId + " rack: " + rackId + " shelf: " + shelfId + " quantity: " + quantity;
	}

	static public Item parse ( String input ) {
		Pattern ITEMID_PATTERN = Pattern.compile("item\\(\"?([A-Z]|[a-z]|[0-9]| )*\"?\\)");
		Pattern RACK_PATTERN = Pattern.compile("rack\\([0-9]+\\)");
		Pattern SHELF_PATTERN = Pattern.compile("shelf\\([0-9]+\\)");
		Pattern QUANTITY_PATTERN = Pattern.compile("quantity\\([0-9]+\\)");

		Matcher matcher = ITEMID_PATTERN.matcher( input );
		String itemId = matcher.find( )
				? matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
				matcher.group( ).length( ) -1 )	: "Error";
		matcher = RACK_PATTERN.matcher( input );
		int rackId = matcher.find( )
				? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
				matcher.group( ).length( ) -1 ) ) : -1;
		matcher = SHELF_PATTERN.matcher( input );
		int shelfId = matcher.find( )
				? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
				matcher.group( ).length( ) -1 ) ) : -1;
		matcher = QUANTITY_PATTERN.matcher( input );
		int quantity = matcher.find( )
				? Integer.parseInt( matcher.group( ).substring( matcher.group( ).indexOf( "(" ) + 1,
				matcher.group( ).length( ) -1 ) ) : -1;
		return new Item( itemId, rackId, shelfId, quantity );
	}
}
