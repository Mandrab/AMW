package view;

import interpackage.Command;
import interpackage.RequestDispatcher;
import org.apache.commons.lang3.tuple.Triple;
import view.utils.GridBagPanelAdder;
import view.utils.ComponentsBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.EXEC_COMMAND;
import static interpackage.RequestHandler.Request.INFO_COMMANDS;

public class CommandPanel extends JPanel {

	private JList<String> commandsIdList;
	private Vector<Command> availableCommands = new Vector<>( );
	private Command selectedCommand;

	public CommandPanel( RequestDispatcher dispatcher ) {

		setLayout( new GridBagLayout( ) );

		// COMPONENTS CREATION
		commandsIdList = ComponentsBuilder.createList( new Vector<>( Collections.emptyList( ) ),
				Math.min( availableCommands.size( ), 25 ), 225 );       // commands's list
		JTextArea commandName = new JTextArea( );                       // command name
		JTextArea commandDescription = new JTextArea( );                // command description
		JList<String> commandVersionsList = ComponentsBuilder.createList( new Vector<>( ),
				Math.min( availableCommands.size( ), 25 ), 225 );       // command's versions list
		JList<String> selectedVersionRequirements = ComponentsBuilder.createList( new Vector<>( ),
				Math.min( availableCommands.size( ), 25 ), 225 );       // version's requirements list
		JTextArea selectedCommandVersion = new JTextArea( );            // version script
		JButton execButton = new JButton( "Exec" );                     // submit button

		// COMPONENTS SETUP
		execButton.setEnabled( false );

		// TODO list requirements

		// COMPONENTS PLACEMENT
		new GridBagPanelAdder( ).setPosition( 0, 0 ).setWideness( 1, 5 ).setWeight( 0.33, 1 ).setPadding( 0, 10, 0, 0 )
				.addToPanel( this, commandsIdList );
		new GridBagPanelAdder( ).setPosition( 1, 0 ).setWideness( 2, 1 ).setWeight( 0.33, 0 )
				.addToPanel( this, commandName );
		new GridBagPanelAdder( ).setPosition( 1, 1 ).setWideness( 2, 1 ).setWeight( 0.33, 0 )
				.addToPanel( this, commandDescription );
		new GridBagPanelAdder( ).setPosition( 1, 2 ).setWideness( 2, 1 ).setWeight( 0.77, 0.5 ).setPadding( 5, 0, 5, 0 )
				.addToPanel( this, commandVersionsList );
		new GridBagPanelAdder( ).setPosition( 1, 3 ).setWeight( 0.33, 0.5 )
				.addToPanel( this, selectedVersionRequirements );
		new GridBagPanelAdder( ).setPosition( 2, 3 ).setWeight( 0.33, 0.5 ).addToPanel( this, selectedCommandVersion );
		new GridBagPanelAdder( ).setPosition( 2, 6 ).setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, execButton );


		// LISTENERS
		commandsIdList.addMouseListener( new MouseAdapter( ) {
			public void mouseClicked( MouseEvent e ) {
				if ( e.getClickCount( ) == 2 ) {
					execButton.setEnabled( false );
					selectedCommand = availableCommands.get( commandsIdList.getSelectedIndex( ) );
					commandName.setText( selectedCommand.getName( ) );
					commandDescription.setText( selectedCommand.getDescription( ) );
					commandVersionsList.setListData( selectedCommand.getVersions( ).stream( ).map( Triple::getLeft )
							.collect( Collectors.toList( ) ).toArray( new String[]{ } ) );
				}
			} } );
		commandVersionsList.addMouseListener( new MouseAdapter( ) {
			public void mouseClicked( MouseEvent e ) {
				if ( e.getClickCount( ) == 2 ) {
					execButton.setEnabled( true );
					selectedCommandVersion.setText( selectedCommand.getVersions( )
							.get( commandVersionsList.getSelectedIndex( ) ).getRight( ) );
					selectedVersionRequirements.setListData( selectedCommand.getVersions( )
							.get( commandVersionsList.getSelectedIndex( ) ).getMiddle( ).toArray( new String[]{ } ) );
				}
			} } );
		execButton.addActionListener( e -> dispatcher.askFor( EXEC_COMMAND, selectedCommand.getId( ),
				commandVersionsList.getSelectedValue( ) ) );

		//
		dispatcher.<CompletableFuture<List<Command>>>askFor( INFO_COMMANDS ).thenAccept( this::update );
	}

	public void update( List<Command> commands ) {
		if ( commands.equals( availableCommands ) ) return;

		availableCommands.clear( );
		availableCommands.addAll( commands );
		commandsIdList.setListData( ( Vector<String> ) availableCommands.stream( )
				.map( Command::getId )
				.collect( Collectors.toCollection( Vector::new ) ) );
	}
}
