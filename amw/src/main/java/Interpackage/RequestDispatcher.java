package Interpackage;

public interface RequestDispatcher extends RequestHandler {

	void register( RequestHandler handler );

	void unregister( RequestHandler handler );
}
