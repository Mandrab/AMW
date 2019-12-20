package Controller;

import InterpackageDatas.Item;
import View.View;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static Controller.Mediator.RequireOntology.WAREHOUSE_STATE;

class UpdaterImpl extends Thread implements Updater {

	private static final int REFRESH_TIME = 100;
	private View view;
	private AgentMediator mediator;
	private boolean suspended;
	private boolean terminated;

	public UpdaterImpl ( View view, AgentMediator mediator ) {
		this.view = view;
		this.mediator = mediator;
	}

	public void run() {
		while (!terminated) {
			try {
				exec();
				synchronized (this) {
					while (suspended) {
						wait();
					}
				}
				Thread.sleep(REFRESH_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void exec( ) {
		mediator.<CompletableFuture<List<Item>>>ask( WAREHOUSE_STATE ).thenAccept( l -> view.update( l ) );
	}

	public synchronized void terminate() {
		if (suspended) {
			suspended = false;
			notifyAll();
		}
		terminated = true;
	}

	public synchronized void setPause(final boolean val) {
		suspended = val;
		if (!suspended) {
			notifyAll();
		}
	}

	public boolean isRunning() {
		return !suspended;
	}

	public boolean isTerminated() {
		return terminated;
	}

}
