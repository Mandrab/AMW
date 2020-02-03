package controller;

import interpackage.RequestDispatcher;
import interpackage.RequestDispatcherImpl;
import model.ModelImpl;
import view.ViewImpl;

public class ControllerImpl implements Controller {

	ControllerImpl ( ) {
		RequestDispatcher dispatcher = new RequestDispatcherImpl( );
		new ModelImpl( dispatcher );
		new ViewUpdaterImpl( new ViewImpl( dispatcher ),
				dispatcher )
				.start( );
	}

}
