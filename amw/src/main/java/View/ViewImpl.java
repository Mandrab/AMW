package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import Interpackage.Item;
import Interpackage.RequestHandler;

import static Interpackage.RequestHandler.Request.*;

public class ViewImpl extends JFrame implements View {

	private RequestHandler supplier;
	private JPanel orderTabPanel;
	private GraphicalWarehouse graphicalWarehouseTab;

	// setup window
	public ViewImpl ( RequestHandler supplier ) {

		this.supplier = supplier;

		graphicalWarehouseTab = new GraphicalWarehouse( this, 10 );

		setupTheme(  );

		setupView(  );
	}

	private void setupTheme(  ) {
		UIManager.put( "control", new Color( 128, 128, 128) );
		UIManager.put( "info", new Color(128,128,128) );
		UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
		UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
		UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
		UIManager.put( "nimbusFocus", new Color(115,164,209) );
		UIManager.put( "nimbusGreen", new Color(176,179,50) );
		UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
		UIManager.put( "nimbusLightBackground", new Color( 18, 30, 49) );
		UIManager.put( "nimbusOrange", new Color(191,98,4) );
		UIManager.put( "nimbusRed", new Color(169,46,34) );
		UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
		UIManager.put( "nimbusSelectionBackground", new Color( 104, 93, 156) );
		UIManager.put( "text", new Color( 230, 230, 230) );
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch ( ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
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
					supplier.askFor( END );
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

		// window panel setup
		orderTabPanel = new JPanel( new GridBagLayout( ) );

		// store items and selected items lists
		Vector<String> items = new Vector<>( );
		Vector<String> selectedItems = new Vector<>( );

		// client name
		new PanelAdder().setPosition( 1, 0 ).setPadding( 0, 0, 0, 10 )
				.addToPanel( orderTabPanel, new JTextArea( "Client" ) );
		JTextField clientInput = new JTextField( "name", 20 );
		new PanelAdder().setPosition( 2, 0 )
				.addToPanel( orderTabPanel, clientInput );

		// client address
		new PanelAdder().setPosition( 1, 1 ).setPadding( 0, 0, 0, 10 )
				.addToPanel( orderTabPanel, new JTextArea( "Address" ) );
		JTextField addressInput = new JTextField( "address", 20 );
		new PanelAdder().setPosition( 2, 1 )
				.addToPanel( orderTabPanel, addressInput );

		// submit
		JButton submitButton = new JButton( "Submit" );
		submitButton.addActionListener( actionEvent -> {
			List<String> l = new ArrayList<>( );
			l.add( clientInput.getText( ) );
			l.add( addressInput.getText( ) );
			l.addAll( Collections.list( selectedItems.elements( ) ) );
			supplier.askFor( ORDER, l.toArray( new String[0] ) );
		} );
		new PanelAdder().setPosition( 2, 25 )
				.addToPanel( orderTabPanel, submitButton );

		// selected items list
		new PanelAdder().setPosition( 1, 2 ).setPadding( 0, 0, 0, 10 )
				.addToPanel( orderTabPanel, new JTextArea( "Items" ) );
		JList<String> selectedItemsList = createList( selectedItems, 10, 225 );
		new PanelAdder().setPosition( 2, 2 ).setWideness( 1, 10 )
				.addToPanel( orderTabPanel, new JScrollPane( selectedItemsList ) );
		JButton removeButton = new JButton( "<-" );
		removeButton.addActionListener( actionEvent -> {
			if ( ! selectedItemsList.isSelectionEmpty( )
					&& selectedItemsList.getSelectedIndex( ) < selectedItems.size( ) ) {
				selectedItems.removeElement( selectedItemsList.getSelectedValue( ) );
				selectedItemsList.setListData( selectedItems );
			} } );
		new PanelAdder().setPosition( 2, 12 )
				.addToPanel( orderTabPanel, removeButton );

		// store list
		JList<String> itemsList = createList( items, Math.min( items.size( ), 25 ), 225 );
		itemsList.setMinimumSize( new Dimension( 50, 50 ) );
		new PanelAdder().setPosition( 0, 0 ).setWideness( 1, 25 )
				.addToPanel( orderTabPanel, new JScrollPane( itemsList ) );
		JButton addButton = new JButton( "->" );
		addButton.addActionListener( actionEvent -> {
			if ( ! itemsList.isSelectionEmpty( )
					&& Arrays.stream( items.toArray( ) ).filter( i -> i.equals( itemsList.getSelectedValue( ) ) ).count( ) >
						Arrays.stream( selectedItems.toArray( ) ).filter( i -> i.equals( itemsList.getSelectedValue( ) ) ).count( ) ) {
				selectedItems.add( itemsList.getSelectedValue( ) );
				selectedItemsList.setListData( selectedItems );
			} } );
		new PanelAdder().setPosition( 0, 25 )
				.addToPanel( orderTabPanel, addButton );

		supplier.<CompletableFuture<List<String>>>askFor( INFO_ITEMS_LIST ).thenAccept( newItems -> {
			items.clear( );
			items.addAll( newItems );
			itemsList.setListData( items );
		} );
	}

	private JList<String> createList( Vector<String> items, int row, int cellW ) {
		JList<String> list = new JList<>( new String[] { "" } );
		list.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		list.setLayoutOrientation( JList.VERTICAL );
		list.setVisibleRowCount( row );
		list.setFixedCellHeight( items.size( ) < 10
				? list.getPreferredSize( ).height * 5
				: Math.min( items.size( ), 25 ) );
		list.setFixedCellWidth( cellW );
		list.setListData( items );
		return list;
	}

	static class PanelAdder {

		private int gridX = 0, gridY = 0, wideX = 1, wideY = 1,
				north = 0, east = 0, south = 0, west = 0;

		PanelAdder setPosition( int gridX, int gridY ) {
			this.gridX = gridX;
			this.gridY = gridY;
			return this;
		}

		PanelAdder setWideness( int wideX, int wideY ) {
			this.wideX = wideX;
			this.wideY = wideY;
			return this;
		}

		PanelAdder setPadding( int north, int east, int south, int west ) {
			this.north = north;
			this.west = west;
			this.south = south;
			this.east = east;
			return this;
		}

		void addToPanel( JPanel panel, Component item ) {
			GridBagConstraints constraints = new GridBagConstraints( );
			constraints.anchor = GridBagConstraints.NORTHEAST;
			constraints.fill = GridBagConstraints.BOTH;
			constraints.gridx = gridX;
			constraints.gridy = gridY;
			constraints.gridwidth = wideX;
			constraints.gridheight = wideY;
			constraints.insets = new Insets( north, west, south, east );
			panel.add( item, constraints );
		}
	}

}
