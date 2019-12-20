package InterpackageDatas;

/*
* This class represent an item in a specific position in the warehouse
* */
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
}
