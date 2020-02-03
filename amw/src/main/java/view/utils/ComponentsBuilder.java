package view.utils;

import javax.swing.*;
import java.util.Vector;

public class ComponentsBuilder {

	public static JList<String> createList( Vector<String> items, int row, int cellW ) {
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
}
