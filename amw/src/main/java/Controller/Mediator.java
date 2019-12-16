package Controller;

import Model.ModelAgent;
import View.View;

public class Mediator {

	public enum CommunicationOntology {
		END, SEND, INPUT, ERROR
	}

	private View view;
	private ModelAgent agent;

	public Mediator( ) { };

	public Mediator( View view, ModelAgent agent ) {
		this.view = view;
		this.agent = agent;
	}

	public void setView ( View view ) {
		this.view = view;
	}

	public void setAgent ( ModelAgent agent ) {
		this.agent = agent;
	}

	public void commandView( CommunicationOntology c, String... args ) {
		if ( view != null )
			view.command( c, args );
		else throw new IllegalStateException( "View unitialized" );
	}

	public void commandAgent( CommunicationOntology c, String... args ) {
		if ( agent != null )
			agent.command( c, args );
		else throw new IllegalStateException( "Agent unitialized" );
	}

}
