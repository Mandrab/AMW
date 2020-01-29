package Controller;

import Interpackage.Item;
import Interpackage.RequestDispatcher;
import View.View;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static Interpackage.RequestHandler.Request.INFO_WAREHOUSE_STATE;

class ViewUpdaterImpl extends Thread implements ViewUpdater {

	private static final int REFRESH_TIME = 100;
	private View view;
	private RequestDispatcher dispatcher;
	private boolean suspended;
	private boolean terminated;

	public ViewUpdaterImpl ( View view, RequestDispatcher dispatcher ) {
		this.view = view;
		this.dispatcher = dispatcher;
	}

	public void run( ) {
		while ( !terminated ) {
			try {
				exec( );
				synchronized ( this ) {
					while ( suspended ) {
						wait( );
					}
				}
				Thread.sleep( REFRESH_TIME );
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
	}

	protected void exec( ) {
		dispatcher.<CompletableFuture<List<Item>>>askFor( INFO_WAREHOUSE_STATE ).thenAccept( l -> view.update( l ) );
	}

	public synchronized void terminate( ) {
		if ( suspended ) {
			suspended = false;
			notifyAll( );
		}
		terminated = true;
	}

	public synchronized void setPause( final boolean val ) {
		suspended = val;
		if ( !suspended ) {
			notifyAll( );
		}
	}

	public boolean isRunning( ) {
		return !suspended;
	}

	public boolean isTerminated( ) {
		return terminated;
	}

}
