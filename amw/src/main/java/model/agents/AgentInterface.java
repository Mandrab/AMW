package model.agents;

import org.junit.runners.model.InitializationError;

public interface AgentInterface {

	void start ( Class agentClass, boolean retryConnection ) throws InitializationError;

}
