package View;

import Controller.Mediator;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import InterpackageDatas.Item;

import static Controller.Mediator.CommandOntology.*;
import static Controller.Mediator.RequireOntology.*;

public class ViewImpl extends JFrame implements View {

	private Mediator mediator;

	private JPanel terminalTabPanel;
	private JPanel orderTabPanel;
	private JPanel graphicTabPanel;
	private JTextPane visualizer;
	private JTextField inputTBox;
	private BiConsumer<String, Color> append;

	// setup window
	public ViewImpl ( Mediator mediator ) {
		this.mediator = mediator;
		mediator.setView( this );

		setupView(  );

		this.append = ( s, c ) -> appendToPane( visualizer, s, c );
	}

	private void setupView(  ) {
		setTitle( "Model.agents.TerminalAg" );

		JTabbedPane tabbedPane = new JTabbedPane( );

		//setupTerminalPanel( );
		//tabbedPane.add( "Terminal", terminalTabPanel );

		setupOrderPanel( );
		tabbedPane.add( "Order", orderTabPanel );

		setupGraphicPanel( );
		tabbedPane.add( "Graphic", graphicTabPanel );

		add( tabbedPane );                                                  // add the panel to this frame

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter(  ) {                            // at close ask and stop the agent
			@Override
			public void windowClosing( WindowEvent windowEvent ) {
				if ( JOptionPane.showConfirmDialog( getParent(),
						"Are you sure you want to close this window?", "Close Window?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION ){
					mediator.commandAgent( END );
					System.exit( 0 );
				}
			}
		});
		pack(  );
		setVisible( true );
		setMinimumSize( new Dimension( 150, 150 ) );
		setLocationRelativeTo( null );
	}

	private void setupTerminalPanel ( ) {

		terminalTabPanel = new JPanel( new BorderLayout(  ) );

		visualizer = new JTextPane( );                                   // create a terminal output
		visualizer.setEditable( false );
		JScrollPane scrollVisualizer = new JScrollPane( visualizer );
		terminalTabPanel.add( scrollVisualizer );

		JPanel sendPanel = new JPanel( new BorderLayout( ) );
		inputTBox = new JTextField( 20 );                                // create a command input area
		inputTBox.addKeyListener( new KeyListener( ) {
			@Override
			public void keyTyped ( KeyEvent keyEvent ) {  }

			@Override
			public void keyPressed ( KeyEvent keyEvent ) {  }

			@Override
			public void keyReleased ( KeyEvent keyEvent ) {
				if ( keyEvent.getKeyCode() == KeyEvent.VK_ENTER )
					sendAction( inputTBox );
			}
		} );
		sendPanel.add( inputTBox );
		JButton sendButton = new JButton( "send" );                                 // add a send button
		sendButton.addActionListener( a -> sendAction( inputTBox ) );
		sendPanel.add( sendButton, BorderLayout.EAST );

		terminalTabPanel.add( sendPanel, BorderLayout.SOUTH );
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
			mediator.commandAgent( SEND, l.toArray( new String[0] ) );
		} );

		orderTabPanel.add( clientPanel );
		orderTabPanel.add( addressPanel );
		orderTabPanel.add( itemsPanel );
		orderTabPanel.add( button );
	}

	private void setupGraphicPanel ( ) {

		graphicTabPanel = new JPanel( new GridLayout( 5,5 ) );

		IntStream.range( 0, 10 ).mapToObj( i -> new JPanel( ) ).forEach( rack -> {
			rack.setBorder( BasicBorders.getTextFieldBorder( ) );
			graphicTabPanel.add( rack );
		} );

		mediator.<CompletableFuture<List<Item>>>askAgent( WAREHOUSE_STATE ).thenAccept( l -> {
			System.out.println( l );
			l.forEach( item -> ( ( JPanel ) graphicTabPanel.getComponent( item.getRackId( ) ) ).
					add( new JTextArea( item.toString( ) ) ) );
		} );
	}

	// action of send a message
	private void sendAction( JTextField iText ) {
		String input = iText.getText(  );
		append.accept( "[User] " + input, Color.BLUE );                             // visualize the command in the output
		iText.setText( "" );

		mediator.commandAgent( SEND, input );
	}

	public void command( Mediator.CommandOntology c, String... args ) {
		if ( c == END ) {
			append.accept( "[Terminal] has stop to work! I'm not able to send anymore message..", Color.RED );
		} else if ( c == SEND ) {
			append.accept( "[" + args[ 0 ]+ "] " + args[ 1 ], Color.BLUE );
		} else if ( c == INPUT ) {
			append.accept( "[" + args[ 0 ] + "] " + args[ 1 ], Color.MAGENTA );
		} else if ( c == Mediator.CommandOntology.ERROR ) {
			append.accept( "[" + args[ 0 ] + "] " + args[ 1 ], Color.GREEN.darker(  ).darker(  ) );
		}
	}

	// modified version of: https://stackoverflow.com/a/9652143
	private void appendToPane( JTextPane tp, String msg, Color c ) {
		try {
			// get attributes
			StyleContext sc = StyleContext.getDefaultStyleContext( );
			AttributeSet aset = sc.addAttribute( SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c );

			// set the style
			aset = sc.addAttribute( aset, StyleConstants.FontFamily, "Lucida Console" );
			aset = sc.addAttribute( aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED );

			// append string
			int len = tp.getDocument( ).getLength( );
			tp.setCaretPosition( len );
			tp.getStyledDocument().insertString( len, msg + System.lineSeparator(  ), aset );

		} catch ( BadLocationException e ) {
			e.printStackTrace( );
		}
	}
}
