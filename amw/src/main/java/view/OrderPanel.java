package view;

import interpackage.RequestDispatcher;
import view.utils.GridBagPanelAdder;
import view.utils.ComponentsBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static interpackage.RequestHandler.Request.INFO_ITEMS_LIST;
import static interpackage.RequestHandler.Request.ORDER;

public class OrderPanel extends JPanel {

	public OrderPanel ( RequestDispatcher dispatcher ) {

		// window panel setup
		setLayout( new GridBagLayout( ) );

		// store items and selected items lists
		Vector<String> items = new Vector<>( );
		Vector<String> selectedItems = new Vector<>( );

		// client name
		new GridBagPanelAdder( ).setPosition( 1, 0 )
				.setPadding( 0, 0, 0, 10 )
				.setWeight( 0.33, 0 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, new JTextArea( "Client" ) );
		JTextField clientInput = new JTextField( "name", 20 );
		new GridBagPanelAdder( ).setPosition( 2, 0 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, clientInput );

		// client address
		new GridBagPanelAdder( ).setPosition( 1, 1 )
				.setPadding( 0, 0, 0, 10 )
				.setWeight( 0.33, 0 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, new JTextArea( "Address" ) );
		JTextField addressInput = new JTextField( "address", 20 );
		new GridBagPanelAdder( ).setPosition( 2, 1 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, addressInput );

		// submit
		JButton submitButton = new JButton( "Submit" );
		submitButton.addActionListener( actionEvent -> {
			if ( selectedItems.size( ) > 0 ) {
				List<String> l = new ArrayList<>( );
				l.add( clientInput.getText( ) );
				l.add( addressInput.getText( ) );
				l.addAll( Collections.list( selectedItems.elements( ) ) );
				dispatcher.askFor( ORDER, l.toArray( new String[ 0 ] ) );
			}
		} );
		new GridBagPanelAdder( ).setPosition( 2, 6 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, submitButton );

		// selected items list
		new GridBagPanelAdder( ).setPosition( 1, 2 )
				.setPadding( 0, 0, 0, 10 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, new JTextArea( "Items" ) );
		JList<String> selectedItemsList = ComponentsBuilder.createList( selectedItems, 10, 225 );
		new GridBagPanelAdder( ).setPosition( 2, 2 )
				.setWeight( 0.33, 1 )
				.addToPanel( this, new JScrollPane( selectedItemsList ) );
		JButton removeButton = new JButton( "<-" );
		removeButton.addActionListener( actionEvent -> {
			if ( ! selectedItemsList.isSelectionEmpty( )
					&& selectedItemsList.getSelectedIndex( ) < selectedItems.size( ) ) {
				selectedItems.removeElement( selectedItemsList.getSelectedValue( ) );
				selectedItemsList.setListData( selectedItems );
			} } );
		new GridBagPanelAdder( ).setPosition( 2, 5 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, removeButton );

		// store list
		JList<String> itemsList = ComponentsBuilder.createList( items, Math.min( items.size( ), 25 ), 225 );
		itemsList.setMinimumSize( new Dimension( 50, 50 ) );
		new GridBagPanelAdder( ).setPosition( 0, 0 )
				.setWideness( 1, 4 )
				.setWeight( 0.33, 1 )
				.addToPanel( this, new JScrollPane( itemsList ) );
		JButton addButton = new JButton( "->" );
		addButton.addActionListener( actionEvent -> {
			if ( ! itemsList.isSelectionEmpty( )
					&& Arrays.stream( items.toArray( ) ).filter( i -> i.equals( itemsList.getSelectedValue( ) ) ).count( ) >
					Arrays.stream( selectedItems.toArray( ) ).filter( i -> i.equals( itemsList.getSelectedValue( ) ) ).count( ) ) {
				selectedItems.add( itemsList.getSelectedValue( ) );
				selectedItemsList.setListData( selectedItems );
			} } );
		new GridBagPanelAdder( ).setPosition( 0, 5 )
				.setFill( GridBagConstraints.HORIZONTAL )
				.addToPanel( this, addButton );

		dispatcher.<CompletableFuture<List<String>>>askFor( INFO_ITEMS_LIST ).thenAccept( newItems -> {
			items.clear( );
			items.addAll( newItems );
			itemsList.setListData( items );
		} );
	}
}
