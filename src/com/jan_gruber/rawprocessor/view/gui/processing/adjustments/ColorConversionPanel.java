package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;

public class ColorConversionPanel extends AdjustmentPanel{
   private boolean isGammaAdjustment; 
   
    public ColorConversionPanel() {
	super();
	this.opName= ProcessingParameters.COLOR_CONVERT;
	
	
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	JPanel colorPanel = new JPanel();
	JPanel gammaPanel = new JPanel();

	JLabel ccLabel = new JLabel("Convert to sRGB");
	ccLabel.setEnabled(true);
	colorPanel.add(ccLabel);

	JLabel gammaLabel = new JLabel("Apply gamma curve");
	gammaPanel.add(gammaLabel);
	JCheckBox gammaCB = new JCheckBox();
	gammaPanel.add(gammaCB);
	gammaCB.addItemListener(new ItemListener() {
	    
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		isGammaAdjustment=!isGammaAdjustment;
	    }
	});

	this.add(colorPanel);
	this.add(gammaPanel);
	
    }

    public boolean isGammaAdjustment() {
        return isGammaAdjustment;
    }

    public void setGammaAdjustment(boolean isGammaAdjustment) {
        this.isGammaAdjustment = isGammaAdjustment;
    }
   
}	
