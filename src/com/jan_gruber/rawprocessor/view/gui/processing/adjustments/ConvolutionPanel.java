package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jan_gruber.rawprocessor.model.engine.processor.ProcessingParameters;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.ConvolutionOp;
import com.mrlonee.swing.animation.AnimatedPanel;

public class ConvolutionPanel extends AdjustmentPanel {
    private JPanel standardPanel;
    private CustomConvolutionPanel customPanel;
    private int convolutionType;
    private int boxSizeX = 3;

    private static HashMap<String, Integer> methodLookUpMap = new HashMap<String, Integer>();
    static {
	methodLookUpMap.put("Simple High Pass",
		ConvolutionOp.HIGHPASS_SHARPENING);
	methodLookUpMap.put("UnsharpMask TODO", ConvolutionOp.UNSHARP_MASKING);
	methodLookUpMap.put("Custom Kernel", ConvolutionOp.CUSTOM_CONVOLUTION);

    }

    public ConvolutionPanel() {
	super();
	this.opName = ProcessingParameters.CONVOLUTION;
	this.setBorder(BorderFactory.createEtchedBorder());
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	standardPanel = new JPanel();


	JComboBox convolutionComboBox = new JComboBox(methodLookUpMap.keySet()
		.toArray(new String[methodLookUpMap.size()]));

	convolutionComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		convolutionType = methodLookUpMap.get(e.getItem());
		if (convolutionType == ConvolutionOp.CUSTOM_CONVOLUTION)
		    customPanel.setVisible(true);
		else
		    customPanel.setVisible(false);
	    }
	});
	standardPanel.add(convolutionComboBox);

	String[] items = { "3x3", "5x5", "7x7" };
	JComboBox boxSizeComboBox = new JComboBox(items);
	boxSizeComboBox.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent arg0) {
		String[] splitResult = ((String) arg0.getItem()).split("x");
		boxSizeX = Integer.valueOf(splitResult[0]);
		customPanel.changeKernelPanel(boxSizeX);
	    }
	});
	standardPanel.add(boxSizeComboBox);
	this.add(standardPanel);

	customPanel = new CustomConvolutionPanel();
	this.add(customPanel);
	customPanel.setVisible(false);
	convolutionComboBox.selectWithKeyChar('C');

    }

    private class CustomConvolutionPanel extends AnimatedPanel {
	int border = 4;
	AnimatedPanel kernelPanel;
	ArrayList<JFormattedTextField> kernelTextFields = new ArrayList<JFormattedTextField>();

	public CustomConvolutionPanel() {
	    super();
	    this.setAnimationType(AnimationType.SlideAnimationFromTop);
	    this.setTransparencyOnAnimation(true);
	    this.setBorder(BorderFactory.createEmptyBorder(border, border,
		    border, border));
	    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	    JLabel kernelLabel = new JLabel("Custom kernel: ");
	    kernelLabel.setAlignmentX(CENTER_ALIGNMENT);
	    this.add(kernelLabel);

	    kernelPanel = createKernelPanel(3, 3);

	    this.add(kernelPanel);

	}

	private void changeKernelPanel(int boxSizeX) {
	    this.remove(kernelPanel);
	    kernelPanel = createKernelPanel(boxSizeX, boxSizeX);
	    kernelPanel.setVisible(false);
	    this.add(kernelPanel);
	    kernelPanel.setVisible(true);
	    revalidate();
	}

	private AnimatedPanel createKernelPanel(int colCount, int rowCount) {
	    AnimatedPanel kernelPanel = new AnimatedPanel();
	    kernelPanel.setLayout(new GridBagLayout());
	    kernelPanel.setTransparencyOnAnimation(true);
	    kernelPanel.setAnimationType(AnimationType.ScaleFromBackground);
	    for (int y = 0; y < rowCount; y++) {
		for (int x = 0; x < colCount; x++) {
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.gridx = x;
		    gbc.gridy = y;
		    gbc.gridwidth = 1;
		    gbc.gridheight = 1;

		    JFormattedTextField customKernelField = new JFormattedTextField(
			    new Float(0.0));
		    //customKernelField.setColumns(2);
		    kernelPanel.add(customKernelField, gbc);
		    kernelTextFields.add(customKernelField);

		}
	    }
	    return kernelPanel;
	}

	private float[] getKernel() {
	    if (convolutionType == ConvolutionOp.CUSTOM_CONVOLUTION) {
		float[] kernelData = new float[boxSizeX * boxSizeX];
		int c = 0;
		for (JFormattedTextField jtf : kernelTextFields) {
		    kernelData[c] = ((Number) jtf.getValue()).floatValue();
		    c++;
		}

		return kernelData;
	    } else
		return null;

	}
    }

    public int getConvolutionType() {
	return convolutionType;
    }

    public void setConvolutionType(int convolutionType) {
	this.convolutionType = convolutionType;
    }

    public int getBoxSizeX() {
	return boxSizeX;
    }

    public void setBoxSizeX(int boxSizeX) {
	this.boxSizeX = boxSizeX;
    }

    public float[] getKernel() {
	return customPanel.getKernel();

    }

}
