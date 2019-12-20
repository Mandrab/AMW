package Controller;

import Model.InterfaceAgent;

public class AgentMediator implements Mediator {

	private InterfaceAgent agent;

	public AgentMediator ( ) { };

	public AgentMediator ( InterfaceAgent agent ) {
		this.agent = agent;
	}

	public void setAgent ( InterfaceAgent agent ) {
		this.agent = agent;
	}

	public void exec( CommandOntology c, String... args ) {
		if ( agent != null )
			agent.command( c, args );
		else throw new IllegalStateException( "Agent uninitialized" );
	}

	public <T> T ask( RequireOntology c ) {
		if ( agent != null )
			return agent.ask( c );
		throw new IllegalStateException( "Agent uninitialized" );
	}

}
