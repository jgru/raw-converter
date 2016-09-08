package com.jan_gruber.rawprocessor.view.gui.panorama;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jan_gruber.rawprocessor.controller.PanoramaController;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.DemosaicingOp;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;

public class ParamPanel extends JPanel {
    private PanoramaController mController;
    private MainFrame f;

    private static HashMap<String, Integer> seamlineAlgorithmLookUpMap = new HashMap<String, Integer>();
    static {
	seamlineAlgorithmLookUpMap.put("Simple cut",
		PanoramaParameters.SIMPLE_CUT);
	seamlineAlgorithmLookUpMap.put("Minimum Error Cut",
		PanoramaParameters.MINIMUM_BOUNDARY_CUT);

    }

    private double alpha = 0.01;
    private double sigma = 2.6;
    private int boxSize = 3;
    private int threshold = 100000;

    private int featherSizeX = 10;
    private int seamlineCode = 1;
    private boolean isVisualizeSeam;

    private JFormattedTextField alphaTextField;
    private JFormattedTextField sigmaTextField;
    private JFormattedTextField thresholdTextField;

    public ParamPanel(PanoramaController mController, MainFrame f) {
	this.mController = mController;
	this.f = f;

	ParameterListener mListener = new ParameterListener();

	this.add(createFeatureParamPanel(mListener));
	this.add(createStitchingParamPanel(mListener));

    }

    JFormattedTextField featherTextField;

    private JPanel createStitchingParamPanel(ParameterListener mListener) {
	JPanel stitchingPanel = new JPanel();
	stitchingPanel.setBorder(BorderFactory
		.createTitledBorder("Stitching Parameters"));
	JLabel featherLabel = new JLabel("Feathering: ");
	stitchingPanel.add(featherLabel);
	featherTextField = new JFormattedTextField(new Integer(featherSizeX));
	featherTextField.addPropertyChangeListener(mListener);
	stitchingPanel.add(featherTextField);
	JLabel suffixLabel = new JLabel("px");
	stitchingPanel.add(suffixLabel);

	JComboBox seamlineComboBox = new JComboBox(seamlineAlgorithmLookUpMap
		.keySet()
		.toArray(new String[seamlineAlgorithmLookUpMap.size()]));
	seamlineComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		seamlineCode = seamlineAlgorithmLookUpMap.get(e.getItem());

	    }
	});
	stitchingPanel.add(seamlineComboBox);

	JButton stitchButton = new JButton(mController.getStichingAction(f,
		this));
	stitchingPanel.add(stitchButton);

	JCheckBox visualizeCheckBox = new JCheckBox();
	stitchingPanel.add(visualizeCheckBox);
	visualizeCheckBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		isVisualizeSeam = !isVisualizeSeam;
	    }
	});
	JLabel visualizeLabel = new JLabel("Visualize seamline?  ");
	stitchingPanel.add(visualizeLabel);
	
	return stitchingPanel;
    }

    private JPanel createFeatureParamPanel(ParameterListener mListener) {
	JPanel featurePanel = new JPanel();
	featurePanel.setBorder(BorderFactory
		.createTitledBorder("Feature Detection Parameters"));
	JLabel alphaLabel = new JLabel("Alpha: ");
	featurePanel.add(alphaLabel);

	alphaTextField = new JFormattedTextField(new Double(alpha));
	alphaTextField.addPropertyChangeListener(mListener);
	featurePanel.add(alphaTextField);
	JLabel sigmaLabel = new JLabel("Sigma: ");
	featurePanel.add(sigmaLabel);
	sigmaTextField = new JFormattedTextField(new Double(sigma));
	sigmaTextField.addPropertyChangeListener(mListener);
	featurePanel.add(sigmaTextField);
	JLabel boxLabel = new JLabel("Smoothing Box Size: ");
	featurePanel.add(boxLabel);
	JComboBox boxSizeComboBox = new JComboBox(new String[] { "3x3","5x5",
		"7x7" });
	boxSizeComboBox.setPreferredSize(new Dimension(75, alphaTextField
		.getPreferredSize().height));
	boxSizeComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		String[] splitResult = ((String) e.getItem()).split("x");
		boxSize = Integer.valueOf(splitResult[0]);
	    }
	});
	featurePanel.add(boxSizeComboBox);

	JLabel thresholdLabel = new JLabel("Threshold: ");
	featurePanel.add(thresholdLabel);
	thresholdTextField = new JFormattedTextField(new Integer(threshold));
	thresholdTextField.addPropertyChangeListener(mListener);
	featurePanel.add(thresholdTextField);
	

	return featurePanel;
    }

    private class ParameterListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    Object source = evt.getSource();
	    if (source.equals(alphaTextField)) {
		alpha = ((Number) alphaTextField.getValue()).doubleValue();
	    } else if (source.equals(sigmaTextField)) {
		sigma = ((Number) sigmaTextField.getValue()).doubleValue();
	    } else if (source.equals(thresholdTextField)) {
		threshold = ((Number) thresholdTextField.getValue()).intValue();
	    }else if(source.equals(featherTextField)){
		featherSizeX= ((Number) featherTextField.getValue()).intValue();
	    }
	}
    }

    public PanoramaParameters getFeatureDetectionParams() {
	System.err.println("return feature params");
	return new PanoramaParameters(alpha, sigma, boxSize, threshold, featherSizeX, seamlineCode,
		isVisualizeSeam);
    }
}
