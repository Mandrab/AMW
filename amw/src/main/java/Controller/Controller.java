package Controller;

import Model.TerminalAg;
import View.ViewImpl;

public class Controller {

	private ViewImpl view;
	private Mediator mediator;

	public static void main ( String[] args ) {
		new Controller(  );
	}

	private Controller(  ) {
		//if ( args.length == 0 || Integer.parseInt( args[0] ) == 0 )
		mediator = new Mediator(  );
		TerminalAg.startAg( mediator );
		view = new ViewImpl( mediator );
		//else
		//	new Controller.Sphinx4();
		//System.out.println( "hello world!" );
	}

}
