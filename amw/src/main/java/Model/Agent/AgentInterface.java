package Model.Agent;

import Interpackage.RequestHandler;
import Model.Model;
import jade.wrapper.StaleProxyException;
import org.junit.runners.model.InitializationError;

public interface AgentInterface extends RequestHandler {

	void start ( ) throws InitializationError;

	void setAgent ( ClientAgent agent );

}
