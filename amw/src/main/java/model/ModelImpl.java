package model;

import interpackage.RequestDispatcher;
import model.agents.AgentInterface;
import model.agents.AgentInterfaceImpl;
import model.agents.admin.AdminAgentImpl;
import model.agents.client.ClientAgentImpl;
import org.junit.runners.model.InitializationError;

public class ModelImpl implements Model {

	public ModelImpl( RequestDispatcher dispatcher, boolean retryConnection ) {
		try {
			new AgentInterfaceImpl( dispatcher ).start( ClientAgentImpl.class, retryConnection );
			new AgentInterfaceImpl( dispatcher ).start( AdminAgentImpl.class, retryConnection );
		} catch ( InitializationError initializationError ) {
			initializationError.printStackTrace( );
		}
	}

}
