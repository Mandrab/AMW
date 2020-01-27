package Model.Agent;

import Interpackage.RequestHandler;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.junit.runners.model.InitializationError;

public class AgentInterfaceImpl implements AgentInterface {

	private ClientAgent agent;                                              // the agent to mediate

	// start the jade agent
	@Override
	public void start ( ) throws InitializationError {
		try {
			Runtime rt = Runtime.instance(  );                                  // get a hold on JADE runtime

			Profile p = new ProfileImpl(  );                                    // create a default profile

			ContainerController cc = rt.createAgentContainer( p );              // create a new non-main container

			AgentController agent =                                             // Create a new agent
					cc.createNewAgent( "interface-ag",
							ClientAgentImpl.class.getCanonicalName( ),
							new Object[]{ this } );

			agent.start(  );                                                    // fire up the agent
		} catch ( StaleProxyException e ) {
			throw new InitializationError( "Failed to initialize the agent" );
		}
	}

	@Override
	public void setAgent ( ClientAgent agent ) {
		this.agent = agent;
	}

	@Override
	public <T> T askFor ( RequestHandler.Request request, String... args ) {
		if ( agent != null )
			return agent.askFor( request, args );
		else throw new IllegalStateException( "Agent uninitialized!" );
	}

}
