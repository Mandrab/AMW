package model.agents;

import interpackage.RequestDispatcher;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.junit.runners.model.InitializationError;

import static interpackage.utils.Utils.sleep;

public class AgentInterfaceImpl implements AgentInterface {

	private RequestDispatcher dispatcher;

	public AgentInterfaceImpl ( RequestDispatcher dispatcher ) {
		this.dispatcher = dispatcher;
	}

	// start the jade agent
	@Override
	public void start ( Class agentClass, boolean retryConnection ) throws InitializationError {

		AgentController agent = null;

		do {
			try {
				Runtime rt = Runtime.instance(  );                                  // get a hold on JADE runtime

				Profile p = new ProfileImpl(  );                                    // create a default profile

				ContainerController cc = rt.createAgentContainer( p );              // create a new non-main container

				agent = cc.createNewAgent( "interface-ag" + String.format( "%.10f", // Create a new agent
						Math.random( ) ), agentClass.getCanonicalName( ), new Object[]{ dispatcher } );

				agent.start( );                                                     // fire up the agent

			} catch ( StaleProxyException se ) {
				throw new InitializationError( "Failed to initialize the agent" );
			} catch ( NullPointerException npe ) {                                  // when the system is not running
				sleep( 1000 );
				if ( ! retryConnection )
					npe.printStackTrace( );
			}
		} while ( agent == null && retryConnection );
	}

}