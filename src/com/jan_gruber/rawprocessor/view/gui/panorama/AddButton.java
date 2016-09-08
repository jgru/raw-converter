package com.jan_gruber.rawprocessor.view.gui.panorama;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

import com.jan_gruber.rawprocessor.view.gui.ColorDefinitions;

public class AddButton extends JButton {
    private Color mColor = ColorDefinitions.baseColor;
    int width = 36;
    int height = 18;
    int padding = 3;
    int plusSize = 8;
    int stroke = 2;

    boolean isAdd = true;

    public AddButton() {
	this.setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);

	g.setColor(mColor);
	if (isAdd) {
	    g.fillRect(width / 2 - stroke / 2, height / 2 - plusSize / 2,
		    stroke, plusSize);
	    g.fillRect(width / 2 - plusSize / 2, height / 2 - stroke / 2,
		    plusSize, stroke);
	} else
	    g.fillRect(width / 2 - plusSize / 2, height / 2 - stroke / 2,
		    plusSize, stroke);
    }

    void changeState() {
	isAdd = !isAdd;
    }
}
