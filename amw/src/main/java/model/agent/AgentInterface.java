package model.agent;

import org.junit.runners.model.InitializationError;

public interface AgentInterface {

	void start ( boolean retryConnection ) throws InitializationError;

}
