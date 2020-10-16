package model;

import interpackage.RequestDispatcher;
import model.agent.AgentInterface;
import model.agent.AgentInterfaceImpl;
import org.junit.runners.model.InitializationError;

public class ModelImpl implements Model {

	private AgentInterface agent;

	public ModelImpl( RequestDispatcher dispatcher, boolean retryConnection ) {
		try {
			agent = new AgentInterfaceImpl( dispatcher );
			agent.start( retryConnection );
		} catch ( InitializationError initializationError ) {
			initializationError.printStackTrace( );
		}
	}

}
