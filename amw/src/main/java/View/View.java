package View;

import Controller.Mediator;

public interface View {

	void command ( Mediator.CommunicationOntology c, String... args );
}
