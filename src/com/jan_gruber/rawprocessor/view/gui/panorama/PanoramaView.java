package com.jan_gruber.rawprocessor.view.gui.panorama;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.jan_gruber.rawprocessor.controller.Controller;
import com.jan_gruber.rawprocessor.controller.PanoramaController;
import com.jan_gruber.rawprocessor.model.engine.io.RawImageReader;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaParameters;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaEditor;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.view.AbstractComponent;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai.TilingViewport;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai.Viewport;
import com.spinn3r.log5j.Logger;

public class PanoramaView extends JComponent implements AbstractComponent,
	ActionListener {
    private static final Logger LOGGER = Logger.getLogger();
    private PanoramaController mController;
    private MainFrame f;
    private AddButton addButton;
    ParamPanel mParamPanel;

    private int index;
    private ArrayList<JPanel> panels;
    private ArrayList<Viewport> viewports;
    private ArrayList<String> fileNames;

    public PanoramaView(PanoramaController mController, MainFrame f) {
	this.mController = mController;
	this.f = f;
	mController.addView(this);
	this.setLayout(new BorderLayout());

	mParamPanel = new ParamPanel(mController, f);
	this.add(mParamPanel, BorderLayout.SOUTH);

	init();

	gridPanel = createGridPanel(mController, f);
	this.add(gridPanel, BorderLayout.CENTER);

	JPanel addPanel = new JPanel();
	addButton = new AddButton();
	addButton.addActionListener(this);
	addButton.setMaximumSize(addButton.getPreferredSize());
	addPanel.add(addButton);
	this.add(addPanel, BorderLayout.EAST);
    }

    private void init() {
	index = -1;

	panels = new ArrayList<JPanel>();
	viewports = new ArrayList<Viewport>();
	fileNames = new ArrayList<String>();

    }

    private JPanel gridPanel;
    private GridBagConstraints gbc;
    private int y = 0;

    private int counter;

    private JPanel createGridPanel(PanoramaController mController, MainFrame f) {
	counter = index;

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	gbc = new GridBagConstraints();

	for (int x = 0; x < 2; x++) {
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = 1;
	    gbc.gridheight = 1;
	    JPanel p = new JPanel();
	    p.setBorder(new TitledBorder("Img " + (++counter + 1)));
	    p.setLayout(new BorderLayout());
	    p.setPreferredSize(new Dimension(650, 380));
	    p.add(new JButton(mController.getIoController()
		    .getFileOpenAction(f)), BorderLayout.CENTER);
	    p.add(new JButton(mController.getFeatureDetectionAction(f,
		    mParamPanel)), BorderLayout.SOUTH);
	    panels.add(p);
	    panel.add(p, gbc);
	}

	return panel;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent e) {
	LOGGER.info("Property Change");

	if (e.getPropertyName().equals(RawImageReader.IMAGE_LOADED)) {
	    LOGGER.info("Panorama image loaded");
	    TilingViewport tv = new TilingViewport(
		    (RenderedImage) e.getNewValue());
	    index++;
	    //remove button
	    panels.get(index).remove(0);
	    //add viewport
	    panels.get(index).add(tv, BorderLayout.CENTER);
	    viewports.add(index, tv);
	    fileNames.add(index,
		    ((RawImageContainer) e.getOldValue()).getName());

	    revalidate();
	    repaint();

	} else if (e.getPropertyName().equals(PanoramaEditor.FEATURE_UPDATE)) {
	    LOGGER.info("Features visualized");
	    if (fileNames.contains(e.getOldValue())) {
		viewports.get(fileNames.indexOf(e.getOldValue())).setOrigImg(
			(RenderedImage) e.getNewValue());
	    }

	} else if (e.getPropertyName().equals(Controller.CONTAINER_REMOVED)) {
	    LOGGER.info("Removing viewport");
	    //restore default view
	    this.remove(gridPanel);
	    init();
	    gridPanel = createGridPanel(this.mController, this.f);
	    this.add(gridPanel, BorderLayout.CENTER);
	    revalidate();
	    repaint();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	if (y < 1) {
	    //resize existing panels
	    for (JPanel p : panels) {
		p.setPreferredSize(new Dimension(650, 300));
	    }
	    //add another row of images
	    y++;
	    for (int x = 0; x < 2; x++) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Img " + (++counter + 1)));
		p.setLayout(new BorderLayout());
		p.setPreferredSize(new Dimension(650, 300));
		p.add(new JButton(this.mController.getIoController()
			.getFileOpenAction(f)), BorderLayout.CENTER);
		p.add(new JButton(this.mController.getFeatureDetectionAction(f,
			mParamPanel)), BorderLayout.SOUTH);
		panels.add(p);
		gridPanel.add(p, gbc);
	    }
	    addButton.changeState();
	} else {
	    //resize existing panels
	    for (JPanel p : panels) {
		p.setPreferredSize(new Dimension(650, 380));
	    }
	    //remove last row
	    y--;

	    for (int x = 0; x < 2; x++) {
		gridPanel.remove(counter);
		panels.remove(counter);
		mController.removeImageContainer(counter);
		counter--;
	    }

	    addButton.changeState();
	}

	gridPanel.revalidate();
	gridPanel.repaint();
	this.repaint();

    }

}
