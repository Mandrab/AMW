package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import interpackage.Command;
import interpackage.Item;
import interpackage.RequestDispatcher;

import static interpackage.RequestHandler.Request.*;

public class ViewImpl extends JFrame implements View {

	private RequestDispatcher dispatcher;
	private OrderPanel orderPanel;
	private WarehousePanel graphicalWarehousePanel;
	private CommandPanel commandPanel;

	// setup window
	public ViewImpl ( RequestDispatcher dispatcher ) {
		this.dispatcher = dispatcher;
		dispatcher.register( this );

		setupTheme(  );

		orderPanel = new OrderPanel( dispatcher );
		graphicalWarehousePanel = new WarehousePanel( 10 );
		commandPanel = new CommandPanel( dispatcher );

		setupView( dispatcher );
	}

	@Override @SuppressWarnings("unchecked")
	public <T> T askFor ( Request request, String... args ) {
		if ( request == CONFIRMATION ) {
			if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( this, "Would you like to confirm the order?",
					"Order Confirmation", JOptionPane.YES_NO_OPTION ) )
				return ( T ) ( Boolean ) true;
			return ( T ) ( Boolean ) false;
		}
		return null;
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

	private void setupView( RequestDispatcher dispatcher ) {
		setTitle( "Model.agents.TerminalAg" );

		JTabbedPane tabbedPane = new JTabbedPane( );

		tabbedPane.add( "Order", orderPanel );

		tabbedPane.add( "Graphic", graphicalWarehousePanel );

		tabbedPane.add( "Command", commandPanel );

		add( tabbedPane );                                                  // add the panel to this frame

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter(  ) {                            // at close ask and stop the agent
			@Override
			public void windowClosing( WindowEvent windowEvent ) {
				if ( JOptionPane.showConfirmDialog( getParent(),
						"Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION ){
					dispatcher.askFor( END );
					System.exit( 0 );
				}
			}
		});
		pack(  );
		setVisible( true );
		setMinimumSize( new Dimension( 150, 150 ) );
		setLocationRelativeTo( null );
	}

	public void update( ) {
		dispatcher.<CompletableFuture<List<Item>>>askFor( INFO_ITEMS_LIST ).thenAccept( state -> orderPanel.update( state ) );
		dispatcher.<CompletableFuture<List<Item>>>askFor( INFO_WAREHOUSE_STATE ).thenAccept( state -> graphicalWarehousePanel.update( state ) );
		dispatcher.<CompletableFuture<List<Command>>>askFor( INFO_COMMANDS ).thenAccept( commands -> commandPanel.update( commands ) );
	}

}
