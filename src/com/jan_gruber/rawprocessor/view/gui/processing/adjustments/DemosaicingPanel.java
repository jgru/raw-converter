package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.DemosaicingOp;

public class DemosaicingPanel extends AdjustmentPanel {
    private final String name = "Demosaicing";
    private int type=2;

    private static HashMap<String, Integer> algorithmLookUpMap = new HashMap<String, Integer>();
    static {
	algorithmLookUpMap.put("Bilinear", DemosaicingOp.TYPE_BILINEAR);
	algorithmLookUpMap.put("Nearest Neighbor",
		DemosaicingOp.TYPE_NEAREST_NEIGHBOUR);
	algorithmLookUpMap.put("Const Hue Based",
		DemosaicingOp.TYPE_HUE_INTERPOLATION);

    }

    public DemosaicingPanel() {
	super();
	opName = ProcessingParameters.DEMOSAICING;
	JLabel label = new JLabel("Choose algorithm ");
	this.add(label);

	JComboBox algorithmComboBox = new JComboBox(algorithmLookUpMap.keySet()
		.toArray(new String[algorithmLookUpMap.size()]));
	algorithmComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		type = algorithmLookUpMap.get(e.getItem());
		System.err.println(type);
	    }
	});
	this.add(algorithmComboBox);
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

}
