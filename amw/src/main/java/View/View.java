package View;

import Controller.Mediator;

public interface View {

	void command ( Mediator.CommandOntology c, String... args );
}
