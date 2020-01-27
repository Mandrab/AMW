package Controller;

import Model.Model;
import Model.ModelImpl;
import View.View;
import View.ViewImpl;

public class ControllerImpl implements Controller {

	private View view;
	private Model model;

	ControllerImpl ( ) {
		model = new ModelImpl( );
		view = new ViewImpl( this );
		Updater updater = new UpdaterImpl( view, model );
		updater.start( );
	}

	@Override
	public <T> T askFor ( Request request, String... args ) {
		return model.askFor( request, args );
	}

}
