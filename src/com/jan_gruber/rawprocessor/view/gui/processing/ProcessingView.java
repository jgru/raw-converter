package com.jan_gruber.rawprocessor.view.gui.processing;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.jan_gruber.rawprocessor.controller.ProcessorController;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.HistogramPanel;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.SideBarPanel;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BottomInfoPanel;

public class ProcessingView extends JSplitPane {
    BasePanel mBasePanel;
    SideBarPanel mSideBarPanel;

    public ProcessingView(ProcessorController mController) {

	HistogramPanel mHistogramPanel = new HistogramPanel();
	BottomInfoPanel mBottomInfoPanel = new BottomInfoPanel();

	mSideBarPanel = new SideBarPanel(mController,
		mHistogramPanel);
	mBasePanel = new BasePanel(mController);
	mBasePanel.setMinimumSize(new Dimension(800, 800));
	setupPropertyChangeAdapter();

	
	JScrollPane jsp = new JScrollPane(mSideBarPanel);
	jsp.setBorder(null);
	jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	jsp.setMaximumSize(mSideBarPanel.getPreferredSize());

	this.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
	this.addImpl(mBasePanel, JSplitPane.LEFT, 0);
	this.addImpl(jsp, JSplitPane.RIGHT, 0);
	
	this.setOneTouchExpandable(false);
	this.setResizeWeight(1.0);
	this.setDividerLocation(1050);
    }

    private void setupPropertyChangeAdapter() {
	mSideBarPanel.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
	    public void propertyChange(PropertyChangeEvent e) {
		// call update methods on BasePanel
		if (e.getPropertyName().equals(BasePanel.VIEWPORT_UPDATE)
			|| e.getPropertyName().equals(
				BasePanel.VIEWPORT_END_UPDATE))
		    ;
		// mBasePanel.updateComponents(e);
	    }
	});

	mBasePanel.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
	    public void propertyChange(PropertyChangeEvent e) {
		// call update methods on AdjustmentPanel
		if (e.getPropertyName().equals(BasePanel.HISTOGRAM_UPDATE))
		    mSideBarPanel.updateComponents(e);
		else if (e.getPropertyName().equals(BasePanel.STATE_UPDATE))
		    mSideBarPanel.updateComponents(e);
	    }
	});

    }

}
