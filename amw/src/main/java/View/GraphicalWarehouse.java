package View;

import Interpackage.Item;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

public class GraphicalWarehouse extends Panel {

	private int rackNumber;

	public GraphicalWarehouse( View view, int racksNumber ) {

		this.rackNumber = racksNumber;

		setLayout( new GridLayout( ( int ) Math.ceil( racksNumber / 2.0 ),
				( int ) Math.ceil( racksNumber / 2.0 ) ) );

		IntStream.range( 0, 10 ).mapToObj( i -> new JPanel( ) ).forEach( rack -> {
			rack.setBorder( BasicBorders.getTextFieldBorder( ) );
			add( rack );
		} );
	}

	public void update( List<Item> items ) {
		IntStream.range( 0, rackNumber ).forEach( i -> {
			if ( getComponent( i ) instanceof JPanel )
				( ( JPanel ) getComponent( i ) ).removeAll( );
		} );
		items.forEach ( item -> ( ( JPanel ) getComponent( item.getRackId( ) ) ).
					add( new JTextArea( item.toString( ) ) ) );
	}
}
