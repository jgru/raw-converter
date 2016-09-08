package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;

public class NoiseReductionPanel extends AdjustmentPanel {
    private String[] boxSizes = { "3x3", "5x5", "7x7", "9x9" };
    private int boxSizeX=3;

    public NoiseReductionPanel() {
	super();
	this.opName = ProcessingParameters.NOISE_REDUCTION;
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	this.setBorder(BorderFactory.createEtchedBorder());
	JLabel filterLabel= new JLabel("Median filtering  ");
	filterLabel.setAlignmentX(CENTER_ALIGNMENT);
	this.add(filterLabel);
	JPanel contentPanel= new JPanel();
	JLabel nrLabel = new JLabel("Choose box size: ");
	contentPanel.add(nrLabel);

	JComboBox boxSizeComboBox = new JComboBox(boxSizes);
	boxSizeComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent arg0) {
		String[] splitResult = ((String) arg0.getItem()).split("x");
		boxSizeX = Integer.valueOf(splitResult[0]);
	    }
	});
	contentPanel.add(boxSizeComboBox);
	this.add(contentPanel);
	
	
    }

    public int getBoxSizeX() {
        return boxSizeX;
    }

    public void setBoxSizeX(int boxSizeX) {
        this.boxSizeX = boxSizeX;
    }
}
