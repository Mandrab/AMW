package interpackage;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class RequestDispatcherImpl implements RequestDispatcher {

	private List<RequestHandler> handlers;

	public RequestDispatcherImpl( ) {
		handlers = new ArrayList<>( );
	}

	@Override
	public void register ( RequestHandler handler ) {
		handlers.add( handler );
	}

	@Override
	public void unregister ( RequestHandler handler ) {
		handlers.remove( handler );
	}

	@Override
	public <T> T askFor ( Request request, String... args ) {
		for ( RequestHandler handler : handlers ) {
			try {
				T res = handler.askFor( request, args );
				if ( res != null )
					return res;
			} catch ( NotImplementedException e ) { }
		}
		return null;
	}
}
