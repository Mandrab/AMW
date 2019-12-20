package Controller;

public interface Mediator {

	enum CommandOntology {
		END, SEND
	}

	enum RequireOntology {
		WAREHOUSE_STATE
	}

	void exec( CommandOntology command, String... args );

	<T> T ask( RequireOntology request );
}
