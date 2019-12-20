package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import Controller.Controller;
import InterpackageDatas.Item;

import static Controller.Mediator.CommandOntology.END;
import static Controller.Mediator.CommandOntology.SEND;

public class ViewImpl extends JFrame implements View {

	private Controller controller;
	private JPanel orderTabPanel;
	private GraphicalWarehouse graphicalWarehouseTab;
	private BiConsumer<String, Color> append;

	// setup window
	public ViewImpl ( Controller controller ) {
		this.controller = controller;

		graphicalWarehouseTab = new GraphicalWarehouse( this, 10 );

		setupView(  );
	}

	private void setupView(  ) {
		setTitle( "Model.agents.TerminalAg" );

		JTabbedPane tabbedPane = new JTabbedPane( );

		setupOrderPanel( );
		tabbedPane.add( "Order", orderTabPanel );

		tabbedPane.add( "Graphic", graphicalWarehouseTab );

		add( tabbedPane );                                                  // add the panel to this frame

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter(  ) {                            // at close ask and stop the agent
			@Override
			public void windowClosing( WindowEvent windowEvent ) {
				if ( JOptionPane.showConfirmDialog( getParent(),
						"Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION ){
					controller.exec( END );
					System.exit( 0 );
				}
			}
		});
		pack(  );
		setVisible( true );
		setMinimumSize( new Dimension( 150, 150 ) );
		setLocationRelativeTo( null );
	}

	public void update( List<Item> items ) {
		 graphicalWarehouseTab.update( items );
	}

	private void setupOrderPanel ( ) {

		orderTabPanel = new JPanel( new GridLayout( 4, 0 ) );

		JPanel clientPanel = new JPanel( );
		clientPanel.add( new JTextArea( "Client" ) );
		JTextField clientInput = new JTextField( "name", 20 );
		clientPanel.add( clientInput );

		JPanel addressPanel = new JPanel(  );
		addressPanel.add( new JTextArea( "Address" ) );
		JTextField addressInput = new JTextField( "address", 20 );
		addressPanel.add( addressInput );

		JPanel itemsPanel = new JPanel(  );
		itemsPanel.add( new JTextArea( "Items (semicolon separated)" ) );
		JTextField itemsInput = new JTextField( "items", 20 );
		itemsPanel.add( itemsInput );

		JButton button = new JButton( "Submit" );
		button.setSize( button.getMinimumSize( ) );
		button.addActionListener( actionEvent -> {
			List<String> l = new ArrayList<>( );
			l.add( clientInput.getText( ) );
			l.add( addressInput.getText( ) );
			l.addAll( Arrays.asList( itemsInput.getText( ).split( ";" ) ) );
			controller.exec( SEND, l.toArray( new String[0] ) );
		} );

		orderTabPanel.add( clientPanel );
		orderTabPanel.add( addressPanel );
		orderTabPanel.add( itemsPanel );
		orderTabPanel.add( button );
	}
}
