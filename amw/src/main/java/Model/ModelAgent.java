package Model;

import Controller.Mediator;

public interface ModelAgent {

	void command ( Mediator.CommandOntology c, String... args );

	<T> T ask ( Mediator.RequireOntology c );
}
