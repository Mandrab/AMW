package View;

import Controller.Mediator;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;

import static Controller.Mediator.CommunicationOntology.*;

public class ViewImpl extends JFrame implements View {

	private Mediator mediator;

	private JPanel mainPanel;
	private JTextPane visualizer;
	private JScrollPane scrollVisualizer;
	private JPanel sendPanel;
	private JTextField inputTBox;
	private JButton sendButton;
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

		mainPanel = new JPanel( new BorderLayout(  ) );

		visualizer = new JTextPane(  );                                   // create a terminal output
		visualizer.setEditable( false );
		scrollVisualizer = new JScrollPane( visualizer );
		mainPanel.add( scrollVisualizer );

		sendPanel = new JPanel( new BorderLayout(  ) );
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
		sendButton = new JButton( "send" );                                 // add a send button
		sendButton.addActionListener( a -> sendAction( inputTBox ) );
		sendPanel.add( sendButton, BorderLayout.EAST );

		mainPanel.add( sendPanel, BorderLayout.SOUTH );

		add( mainPanel );                                                     // add the panel to this frame

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

	// action of send a message
	private void sendAction( JTextField iText ) {
		String input = iText.getText(  );
		append.accept( "[User] " + input, Color.BLUE );                             // visualize the command in the output
		iText.setText( "" );

		mediator.commandAgent( SEND, input );
	}

	public void command( Mediator.CommunicationOntology c, String... args ) {
		if ( c == END ) {
			append.accept( "[Terminal] has stop to work! I'm not able to send anymore message..", Color.RED );
		} else if ( c == SEND ) {
			append.accept( "[" + args[ 0 ]+ "] " + args[ 1 ], Color.BLUE );
		} else if ( c == INPUT ) {
			append.accept( "[" + args[ 0 ] + "] " + args[ 1 ], Color.MAGENTA );
		} else if ( c == Mediator.CommunicationOntology.ERROR ) {
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
