package View;

import Interpackage.Item;
import Interpackage.RequestHandler;

import java.util.List;

public interface View extends RequestHandler {

	void update( List<Item> items );
}
