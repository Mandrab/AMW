package Interpackage;

import org.apache.commons.lang3.NotImplementedException;

public interface RequestHandler {

	enum Request {
		INFO_WAREHOUSE_STATE,                           // require id, position and quantity of the items in the warehouse
		INFO_ITEMS_LIST,                                // require items and quantity
		CONFIRMATION,                                   // ask for order confirm
		END,                                            // ask for system shutdown
		ORDER                                           // ask for make an order
	}

	<T> T askFor ( Request request, String... args ) throws NotImplementedException;

}
