package controller;

import interpackage.RequestDispatcher;
import interpackage.RequestDispatcherImpl;
import model.ModelImpl;
import view.ViewImpl;

public class ControllerImpl implements Controller {

	ControllerImpl ( boolean retryConnection ) {
		RequestDispatcher dispatcher = new RequestDispatcherImpl( );
		new ModelImpl( dispatcher, retryConnection );
		new ViewUpdaterImpl( new ViewImpl( dispatcher ),
				dispatcher )
				.start( );
	}

}
