package view;

import interpackage.Item;
import org.apache.commons.lang3.tuple.ImmutablePair;
import view.utils.GridBagPanelAdder;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WarehousePanel extends JPanel {

	private JPanel graphicPanel;
	private JTextArea textualArea;
	private int rackNumber;

	public WarehousePanel ( int racksNumber ) {

		this.rackNumber = racksNumber;

		setLayout( new GridBagLayout( ) );
		setVisible( true );

		graphicPanel = new JPanel( new GridLayout(
				( int ) Math.floor( racksNumber / 2.0 ),
				( int ) Math.floor( racksNumber / 2.0 ) ) );
		new GridBagPanelAdder( ).setPosition( 0, 0 )
				.setWeight( 0.85, 0.5 )
				.addToPanel( this, graphicPanel );

		textualArea = new JTextArea( );
		new GridBagPanelAdder( ).setPosition( 1, 0 )
				.setWeight( 0.15, 0.5 )
				.addToPanel( this, textualArea );

		IntStream.range( 0, 10 ).mapToObj( i -> new JPanel( ) ).forEach( rack -> {
			rack.setBorder( BasicBorders.getTextFieldBorder( ) );
			rack.setLayout( new BoxLayout( rack, BoxLayout.Y_AXIS ) );
			graphicPanel.add( rack );
		} );
	}

	public void update( List<Item> items ) {
		IntStream.range( 0, rackNumber ).forEach( i -> {
			if ( graphicPanel.getComponent( i ) instanceof JPanel ) {
				JPanel rackPanel = ( JPanel ) graphicPanel.getComponent( i );
				rackPanel.removeAll( );
				JTextArea title = new JTextArea( "Rack number: " + i );
				title.setBackground( new Color( 0, 0, 255 ) );
				rackPanel.add( title );
			}
		} );
		//System.out.println( items );
		//System.out.println(  );
		items.forEach( item -> item.getPositions( ).forEach( pos -> {
				JPanel rackPanel = ( JPanel ) graphicPanel.getComponent( pos.getLeft( ) );
				JTextArea shelfN = new JTextArea( "Shelf number: " + pos.getMiddle( ) );
				shelfN.setBackground( new Color( 60, 80, 245 ) );
				rackPanel.add( shelfN );
				JTextArea itemInfo = new JTextArea( item.getItemId( ) + ", " + pos.getRight( ) );
				itemInfo.setBackground( new Color( 60, 125, 245 ) );
				rackPanel.add( itemInfo );
			} ) );

		textualArea.setText( items.stream( ).map( i -> i.toString( ) + "\n" )
				.collect( Collectors.joining( ) ) );
	}
}
