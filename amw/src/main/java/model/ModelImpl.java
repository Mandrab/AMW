package model;

import interpackage.RequestDispatcher;
import model.agent.AgentInterface;
import model.agent.AgentInterfaceImpl;
import org.junit.runners.model.InitializationError;

public class ModelImpl implements Model {

	private AgentInterface agent;

	public ModelImpl( RequestDispatcher dispatcher ) {
		try {
			agent = new AgentInterfaceImpl( dispatcher );
			agent.start( );
		} catch ( InitializationError initializationError ) {
			initializationError.printStackTrace( );
		}
	}

}
