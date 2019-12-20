package Controller;

import Model.InterfaceAgentImpl;
import View.ViewImpl;

public class Controller implements Mediator {

	private ViewImpl view;
	private AgentMediator mediator;
	private Updater updater;

	public static void main ( String[] args ) {
		new Controller(  );
	}

	private Controller(  ) {
		mediator = new AgentMediator(  );
		InterfaceAgentImpl.startAg( mediator );
		view = new ViewImpl( this );
		updater = new UpdaterImpl( view, mediator );
		updater.start( );
	}

	@Override
	public void exec ( CommandOntology command, String... args ) {
		mediator.exec( command, args );
	}

	@Override
	public <T> T ask ( RequireOntology request ) {
		return mediator.ask( request );
	}
}
