package Model;

import Model.Agent.AgentInterface;
import Model.Agent.AgentInterfaceImpl;
import org.junit.runners.model.InitializationError;

public class ModelImpl implements Model {

	private AgentInterface agent;

	public ModelImpl( ) {
		try {
			agent = new AgentInterfaceImpl( );
			agent.start( );
		} catch ( InitializationError initializationError ) {
			initializationError.printStackTrace( );
		}
	}

	@Override
	public <T> T askFor ( Request request, String... args ) {
		return agent.askFor( request, args );
	}

}
