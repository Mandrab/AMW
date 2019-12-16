package Model;

import Controller.Mediator;

public interface ModelAgent {

	void command ( Mediator.CommunicationOntology c, String... args );
}
