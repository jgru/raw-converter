package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jan_gruber.rawprocessor.controller.ProcessorController;
import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.store.CR2ImageContainer;
import com.jan_gruber.rawprocessor.view.AbstractComponent;
import com.jan_gruber.rawprocessor.view.gui.UpdatableComponent;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.spinn3r.log5j.Logger;

public class SideBarPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger();
    public final Dimension prefSize = new Dimension(320, 1600);
    private ProcessorController pController;
    private JProgressBar mProgressBar;
    FoldableAdjustmentPanelContainer mPanelContainer;

    private ArrayList<UpdatableComponent> componentsToUpdate = new ArrayList<UpdatableComponent>();
    private ArrayList<AdjustmentPanel> adjustmentsComponents = new ArrayList<AdjustmentPanel>();

    public SideBarPanel(ProcessorController pController,
	    HistogramPanel mHistogramPanel) {
	this.pController = pController;
	//necessary for splitpane
	this.setMinimumSize(new Dimension(0, 0));
	this.setPreferredSize(prefSize);
	this.setMaximumSize(prefSize);
	this.setLayout(new FlowLayout());

	componentsToUpdate.add(mHistogramPanel);
	this.add(Box.createRigidArea(new Dimension(320, 15)));
	this.add(mHistogramPanel);

	//setup foldable adjustments
	createFoldablePanelContainer();

	LOGGER.info("Sidebar with adjustments created");

    }

    private void createFoldablePanelContainer() {
	mPanelContainer = new FoldableAdjustmentPanelContainer();
	componentsToUpdate.add(mPanelContainer);

	adjustmentsComponents.add(new PreconditioningPanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Preconditioning");

	adjustmentsComponents.add(new WhiteBalancePanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"White Balance");

	adjustmentsComponents.add(new DemosaicingPanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Demosaicing");

	adjustmentsComponents.add(new ColorConversionPanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Color Conversion");

	adjustmentsComponents.add(new CurvesAdjustmentPanel(300));
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Curves");

	adjustmentsComponents.add(new NoiseReductionPanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Noise Reduction");

	adjustmentsComponents.add(new ConvolutionPanel());
	mPanelContainer.addPanel(
		adjustmentsComponents.get(adjustmentsComponents.size() - 1),
		"Convolution");

	this.add(mPanelContainer.getComponent());

	this.add(setupProgressPanel());
    }

    private Component setupProgressPanel() {
	JPanel progressPanel = new JPanel();
	progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

	progressPanel.add(Box.createRigidArea(new Dimension(5, 35)));

	mProgressBar = new JProgressBar();
	mProgressBar.setEnabled(false);
	progressPanel.add(mProgressBar);
	JButton processButton = new JButton(pController.getProcessAction(this));
	processButton.setAlignmentX(CENTER_ALIGNMENT);
	progressPanel.add(processButton);
	return progressPanel;
    }

    public ProcessingParameters getProcessingParameters() {
	LOGGER.info("create ProcessingParameters");
	ProcessingParameters pm = new ProcessingParameters();

	for (AdjustmentPanel ap : adjustmentsComponents) {
	    if (ap.isActivated()) {
		String opName = ap.getOpName();

		if (opName.equals(ProcessingParameters.PRECONDITIONING)) {
		    pm.addOperation(ap.getOpName());
		    pm.setBlackLevelCompensation(((PreconditioningPanel) ap)
			    .isBlackLevel());
		    pm.setNormalization(((PreconditioningPanel) ap)
			    .isNormalize());

		} else if (opName.equals(ProcessingParameters.WHITEBALANCING)) {
		    //exceptional, whitebalancing belongs to preconditioning
		    pm.addOperation(ProcessingParameters.PRECONDITIONING);
		    pm.setWhiteBalancing(true);
		    pm.setWbType(((WhiteBalancePanel) ap).getWhiteBalanceType());

		} else if (opName.equals(ProcessingParameters.DEMOSAICING)) {
		    pm.addOperation(ap.getOpName());
		    pm.setDemosaicType(((DemosaicingPanel) ap).getType());
		    
		} else if (opName.equals(ProcessingParameters.COLOR_CONVERT)) {
		    pm.addOperation(ap.getOpName());
		    pm.setGammaAdjustment(((ColorConversionPanel) ap).isGammaAdjustment());

		} else if (opName.equals(ProcessingParameters.LUT)) {
		    pm.addOperation(ap.getOpName());
		    pm.setLUT(((CurvesAdjustmentPanel) ap).getLUT());

		} else if (opName.equals(ProcessingParameters.NOISE_REDUCTION)) {
		    pm.addOperation(ap.getOpName());
		    pm.setNoiseReductionBoxSize(((NoiseReductionPanel) ap)
			    .getBoxSizeX());

		} else if (opName.equals(ProcessingParameters.CONVOLUTION)) {
		    pm.addOperation(ap.getOpName());
		    pm.setConvolutionBoxSize(((ConvolutionPanel) ap)
			    .getBoxSizeX());
		    pm.setConvolutionType(((ConvolutionPanel) ap)
			    .getConvolutionType());
		    pm.setCustomConvolutionKernel(((ConvolutionPanel) ap)
			    .getKernel());

		}

	    }

	}
	mProgressBar.setIndeterminate(true);
	return pm;
    }



    //adaptor method triggered by anonymous propertyChangeListener in MainFrame
    public void updateComponents(PropertyChangeEvent e) {
	if (e.getPropertyName().equals(BasePanel.STATE_UPDATE))
	    mProgressBar.setIndeterminate(false);

	// distribute event to owned components
	for (UpdatableComponent c : componentsToUpdate) {
	    c.updateComponent(e);
	}

    }

    @Override
    public Dimension getPreferredSize() {
	return prefSize;
    }

}
