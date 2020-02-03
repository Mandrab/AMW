package view;

import interpackage.Item;
import interpackage.RequestHandler;

import java.util.List;

public interface View extends RequestHandler {

	void update( List<Item> items );
}
