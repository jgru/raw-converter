package com.jan_gruber.rawprocessor.view.gui.processing.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.spinn3r.log5j.Logger;

public class BottomInfoPanel extends JPanel implements PropertyChangeListener {
	private static final Logger LOGGER = Logger.getLogger();
	private static final long serialVersionUID = 1L;
	BasePanel mBasePanel;
	JLabel placeHolderLabel;
	JLabel zoomLabel;

	public BottomInfoPanel() {
		super();
		zoomLabel = new JLabel("-#-" + "100%" + " -#-");
		this.add(zoomLabel);
	}

	

	public void updateZoomLabel(int magnifyInPercent) {
		zoomLabel.setText("-#- " + magnifyInPercent + "%" + " -#-");

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(BasePanel.INFO_UPDATE)) {
			updateZoomLabel((Integer) evt.getNewValue());
		}
	}
}
