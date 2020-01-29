package View;

import Interpackage.Item;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

public class GraphicalWarehouse extends Panel {

	private int rackNumber;

	public GraphicalWarehouse( int racksNumber ) {

		this.rackNumber = racksNumber;

		setLayout( new GridLayout( ( int ) Math.ceil( racksNumber / 2.0 ),
				( int ) Math.ceil( racksNumber / 2.0 ) ) );

		IntStream.range( 0, 10 ).mapToObj( i -> new JPanel( ) ).forEach( rack -> {
			rack.setBorder( BasicBorders.getTextFieldBorder( ) );
			rack.setLayout( new BoxLayout( rack, BoxLayout.Y_AXIS ) );
			add( rack );
		} );
	}

	public void update( List<Item> items ) {
		IntStream.range( 0, rackNumber ).forEach( i -> {
			if ( getComponent( i ) instanceof JPanel ) {
				JPanel rackPanel = ( JPanel ) getComponent( i );
				rackPanel.removeAll( );
				JTextArea title = new JTextArea( "Rack number: " + i );
				title.setBackground( new Color( 0, 0, 255 ) );
				rackPanel.add( title );
			}
		} );
		items.stream( ).sorted( ( i1, i2 ) -> i1.first( i2 ) ).forEach ( item -> {
			JPanel rackPanel = ( JPanel ) getComponent( item.getRackId( ) );
			JTextArea shelfN = new JTextArea( "Shelf number: " + item.getShelfId( ) );
			shelfN.setBackground( new Color( 60, 80, 245 ) );
			rackPanel.add( shelfN );
			JTextArea itemInfo = new JTextArea( item.getItemId( ) + ", " + item.getQuantity( ) );
			itemInfo.setBackground( new Color( 60, 125, 245 ) );
			rackPanel.add( itemInfo );
		} );
	}
}
