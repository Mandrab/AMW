package view.panels;

import asl_actions.load_commands;
import interpackage.RequestDispatcher;
import view.utils.GridBagPanelAdder;

import javax.swing.*;

import java.awt.*;
import java.util.List;

import static interpackage.RequestHandler.Request.EXEC_SCRIPT;

public class ScriptPanel extends JPanel {

	public ScriptPanel( RequestDispatcher dispatcher ) {

		setLayout( new GridBagLayout( ) );

		JTextArea script = new JTextArea( );                            // script
		new GridBagPanelAdder( ).setPosition( 0, 0 ).setWideness( 5, 5 ).setWeight( 1, 0.9 ).setPadding( 0, 0, 10, 0 )
				.addToPanel( this, script );

		JButton execButton = new JButton( "Run!" );                     // submit button
		new GridBagPanelAdder( ).setPosition( 4, 5 ).setFill( GridBagConstraints.VERTICAL )
				.addToPanel( this, execButton );

		execButton.addActionListener( e -> {
			try {
				dispatcher.askFor( EXEC_SCRIPT, requirementsAndScript( script.getText( ) ) );
			} catch ( Exception ex ) {
				ex.printStackTrace( );
				JOptionPane.showMessageDialog( this, "Your input isn't compatible with the application...",
						"Input error!", JOptionPane.ERROR_MESSAGE );
			}
		} );
	}

	private String[] requirementsAndScript( String s ) {
		List<String> list = load_commands.getRequirements( s );
		list.add( 0, load_commands.getScript( s ) );

		return list.toArray( new String[] { } );
	}
}
