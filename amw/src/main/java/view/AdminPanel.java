package view;

import interpackage.Command;
import interpackage.RequestDispatcher;
import view.panels.CommandPanel;
import view.panels.OrderPanel;
import view.panels.ScriptPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

	private OrderPanel orderPanel;
	private CommandPanel commandPanel;
	private ScriptPanel scriptPanel;

	// setup window
	public AdminPanel ( RequestDispatcher dispatcher ) {

		orderPanel = new OrderPanel( dispatcher );
		commandPanel = new CommandPanel( dispatcher );
		scriptPanel = new ScriptPanel( dispatcher );

		setupView( );
	}

	private void setupView( ) {

		setLayout( new BorderLayout( ) );

		JTabbedPane tabbedPane = new JTabbedPane( );
		tabbedPane.setTabPlacement( SwingConstants.LEFT );

		tabbedPane.add( "Order", orderPanel );

		tabbedPane.add( "Command", commandPanel );

		tabbedPane.add( "Script", scriptPanel );


		add( tabbedPane, BorderLayout.CENTER );
	}

	public void update( List<Command> commands ) {
		commandPanel.update( commands );
	}
}
