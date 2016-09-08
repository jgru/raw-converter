package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;

public class PreconditioningPanel extends AdjustmentPanel {
    JCheckBox blackLevelCB;
    JCheckBox normalizeCB;

    boolean isBlackLevel;
    boolean isNormalize;

    public PreconditioningPanel() {
	super();
	opName = ProcessingParameters.PRECONDITIONING;
	CheckBoxChangeListener mChangeListener = new CheckBoxChangeListener();
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	JPanel blackPanel = new JPanel();
	JPanel normPanel = new JPanel();

	JLabel blackLevelLabel = new JLabel("Correct Black Level");
	blackLevelLabel.setEnabled(true);
	
	blackPanel.add(blackLevelLabel);
	blackLevelCB = new JCheckBox();
	blackPanel.add(blackLevelCB);
	blackLevelCB.addChangeListener(mChangeListener);
	blackLevelCB.setSelected(true);
	this.add(blackPanel);

	JLabel normalizeLabel = new JLabel("Normalize tonal range");
	normPanel.add(normalizeLabel);
	normalizeCB = new JCheckBox();
	normPanel.add(normalizeCB);
	normalizeCB.addChangeListener(mChangeListener);
	normalizeCB.setSelected(true);

	this.add(normPanel);

    }

    class CheckBoxChangeListener implements ChangeListener {

	@Override
	public void stateChanged(ChangeEvent e) {
	    if (e.getSource().equals(blackLevelCB))
		isBlackLevel = ((AbstractButton) e.getSource()).isSelected();
	    else if (e.getSource().equals(normalizeCB))
		isNormalize = ((AbstractButton) e.getSource()).isSelected();

	}

    }

    public boolean isBlackLevel() {
	return isBlackLevel;
    }

    public void setBlackLevel(boolean isBlackLevel) {
	this.isBlackLevel = isBlackLevel;
    }

    public boolean isNormalize() {
	return isNormalize;
    }

    public void setNormalize(boolean isNormalize) {
	this.isNormalize = isNormalize;
    }

}
