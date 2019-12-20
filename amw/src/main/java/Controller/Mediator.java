package Controller;

import Model.ModelAgent;
import View.View;

public class Mediator {

	public enum CommandOntology {
		END, SEND, INPUT, ERROR
	}

	public enum RequireOntology {
		WAREHOUSE_STATE
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

	public void commandView( Mediator.CommandOntology c, String... args ) {
		if ( view != null )
			view.command( c, args );
		else throw new IllegalStateException( "View uninitialized" );
	}

	public void commandAgent( Mediator.CommandOntology c, String... args ) {
		if ( agent != null )
			agent.command( c, args );
		else throw new IllegalStateException( "Agent uninitialized" );
	}

	public <T> T askAgent( Mediator.RequireOntology c ) {
		if ( agent != null )
			return agent.ask( c );
		throw new IllegalStateException( "Agent uninitialized" );
	}

}
