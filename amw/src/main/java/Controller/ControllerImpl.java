package Controller;

import Interpackage.RequestDispatcher;
import Interpackage.RequestDispatcherImpl;
import Model.ModelImpl;
import View.ViewImpl;

public class ControllerImpl implements Controller {

	ControllerImpl ( ) {
		RequestDispatcher dispatcher = new RequestDispatcherImpl( );
		new ModelImpl( dispatcher );
		new ViewUpdaterImpl( new ViewImpl( dispatcher ),
				dispatcher )
				.start( );
	}

}
