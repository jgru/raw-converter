package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.PreConditioningOp;

public class WhiteBalancePanel extends AdjustmentPanel {
    private int wbType;
    private static HashMap<String, Integer> wbLookUpMap = new HashMap<String, Integer>();
    static {
	wbLookUpMap.put("As shot", PreConditioningOp.WB_AS_SHOT);
	wbLookUpMap.put("Auto", PreConditioningOp.WB_AUTO);
	wbLookUpMap.put("Daylight", PreConditioningOp.WB_DAYLIGHT);
	wbLookUpMap.put("Cloudy", PreConditioningOp.WB_CLOUDY);
	wbLookUpMap.put("Shade", PreConditioningOp.WB_SHADE);
	wbLookUpMap.put("Tungsten", PreConditioningOp.WB_TUNGSTEN);

    }

    public WhiteBalancePanel() {
	super();
	opName = ProcessingParameters.WHITEBALANCING;

	JLabel label = new JLabel("Color Temperature");
	this.add(label);
	
	JComboBox wbComboBox = new JComboBox(wbLookUpMap.keySet()
		.toArray(new String[wbLookUpMap.size()]));
	
	wbComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		wbType = wbLookUpMap.get(e.getItem());
	    }
	});
	this.add(wbComboBox);

    }

    public int getWhiteBalanceType() {
        return wbType;
    }

    public void setWhiteBalanceType(int wbType) {
        this.wbType = wbType;
    }

}
