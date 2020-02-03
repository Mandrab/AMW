package view.utils;

import javax.swing.*;
import java.awt.*;

public class GridBagPanelAdder {

	private int gridX = 0, gridY = 0, wideX = 1, wideY = 1,
			north = 0, east = 0, south = 0, west = 0;
	private double weightX = 0, weightY = 0;
	private int fill = GridBagConstraints.BOTH;

	public GridBagPanelAdder setFill ( int fill ) {
		this.fill = fill;
		return this;
	}

	public GridBagPanelAdder setPadding( int north, int east, int south, int west ) {
		this.north = north;
		this.west = west;
		this.south = south;
		this.east = east;
		return this;
	}

	public GridBagPanelAdder setPosition( int gridX, int gridY ) {
		this.gridX = gridX;
		this.gridY = gridY;
		return this;
	}

	public GridBagPanelAdder setWeight( double weightX, double weightY ) {
		this.weightX = weightX;
		this.weightY = weightY;
		return this;
	}

	public GridBagPanelAdder setWideness( int wideX, int wideY ) {
		this.wideX = wideX;
		this.wideY = wideY;
		return this;
	}

	public void addToPanel( JPanel panel, Component item ) {
		GridBagConstraints constraints = new GridBagConstraints( );
		constraints.anchor = GridBagConstraints.NORTHEAST;
		constraints.fill = fill;
		constraints.weightx = weightX;
		constraints.weighty = weightY;
		constraints.gridx = gridX;
		constraints.gridy = gridY;
		constraints.gridwidth = wideX;
		constraints.gridheight = wideY;
		constraints.insets = new Insets( north, west, south, east );
		panel.add( item, constraints );
	}
}
