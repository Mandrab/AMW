package view;

import interpackage.Command;
import interpackage.RequestDispatcher;
import view.utils.GridBagPanelAdder;
import view.utils.ComponentsBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static interpackage.RequestHandler.Request.INFO_COMMANDS;

public class CommandPanel extends JPanel {

	private Command selectedCommand;

	public CommandPanel( RequestDispatcher dispatcher ) {

		Vector<Command> availableCommands = new Vector<>( );

		setLayout( new GridBagLayout( ) );

		// command name
		JTextArea commandName = new JTextArea( );
		new GridBagPanelAdder( ).setPosition( 1, 0 )
				.setWideness( 2, 1 )
				.setWeight( 0.33, 0 )
				.addToPanel( this, commandName );

		// command description
		JTextArea commandDescription = new JTextArea( );
		new GridBagPanelAdder( ).setPosition( 1, 1 )
				.setWideness( 2, 1 )
				.setWeight( 0.33, 0 )
				.addToPanel( this, commandDescription );

		// version script
		JTextArea versionScript = new JTextArea( );
		new GridBagPanelAdder( ).setPosition( 2, 3 )
				.setWeight( 0.33, 0.5 )
				.addToPanel( this, versionScript );

		// TODO list requirements
		// requirements's versions list
		JList<String> versionRequirements = ComponentsBuilder.createList( new Vector<>( ),
				Math.min( availableCommands.size( ), 25 ), 225 );
		new GridBagPanelAdder( ).setPosition( 1, 3 )
				.setWeight( 0.33, 0.5 )
				.addToPanel( this, versionRequirements );

		// command's versions list
		JList<String> commandVersions = ComponentsBuilder.createList( new Vector<>( ),
				Math.min( availableCommands.size( ), 25 ), 225 );
		commandVersions.addMouseListener( new MouseAdapter( ) {
			public void mouseClicked( MouseEvent e ) {
				if ( e.getClickCount( ) == 2 ) {
					versionScript.setText( selectedCommand.getVersions( )
							.get( commandVersions.getSelectedIndex( ) ).getValue( ) );
					versionRequirements.setListData( selectedCommand.getVersions( )
							.get( commandVersions.getSelectedIndex( ) ).getKey( ).toArray( new String[]{ } ) );
				}
			} } );
		new GridBagPanelAdder( ).setPosition( 1, 2 )
				.setWideness( 2, 1 )
				.setWeight( 0.77, 0.5 )
				.setPadding( 5, 0, 5, 0 )
				.addToPanel( this, commandVersions );

		JButton execButton = new JButton( "Submit" );
		new GridBagPanelAdder( ).setPosition( 2, 6 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, execButton );

		// commands's list
		JList<String> commandsNames = ComponentsBuilder.createList( new Vector<>( Arrays.asList( ) ),
				Math.min( availableCommands.size( ), 25 ), 225 );
		commandsNames.addMouseListener( new MouseAdapter( ) {
			public void mouseClicked( MouseEvent e ) {
				if ( e.getClickCount( ) == 2 ) {
					selectedCommand = availableCommands.get( commandsNames.getSelectedIndex( ) );
					commandName.setText( selectedCommand.getName( ) );
					commandDescription.setText( selectedCommand.getDescription( ) );
					commandVersions.setListData( selectedCommand.getVersions( ).stream( ).map( i -> "Variant" )
							.collect( Collectors.toList( ) ).toArray( new String[]{ } ) );
				}
			} } );
		new GridBagPanelAdder( ).setPosition( 0, 0 )
				.setWideness( 1, 5 )
				.setWeight( 0.33, 1 )
				.setPadding( 0, 10, 0, 0 )
				.addToPanel( this, commandsNames );
		dispatcher.<CompletableFuture<List<Command>>>askFor( INFO_COMMANDS ).thenAccept( newCommand -> {// TODO non arriva
			availableCommands.clear( );
			availableCommands.addAll( newCommand );
			commandsNames.setListData( ( Vector<String> ) availableCommands.stream( )
					.map( Command::getId )
					.collect( Collectors.toCollection( Vector::new ) ) );
		} );
	}
}
