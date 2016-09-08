package com.jan_gruber.rawprocessor.view.gui.processing.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jan_gruber.rawprocessor.controller.Controller;
import com.jan_gruber.rawprocessor.model.engine.io.RawImageReader;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.view.AbstractComponent;
import com.jan_gruber.rawprocessor.view.gui.processing.adjustments.SideBarPanel;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai.TilingViewport;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.jai.Viewport;
import com.spinn3r.log5j.Logger;

public class BasePanel extends JPanel implements AbstractComponent,
	PropertyChangeListener {
    private static final long serialVersionUID = 8707557253890955479L;
    private static final Logger LOGGER = Logger.getLogger();
    public static final String HISTOGRAM_UPDATE = "histogramUpdate";
    public static final String VIEWPORT_UPDATE = "viewportUpdate";
    public static final String VIEWPORT_END_UPDATE = "viewportEndUpdate";
    public static final String INFO_UPDATE = "infoUpdate";
    public static final String STATE_UPDATE = "stateUpdate";

    private ArrayList<Viewport> viewports = new ArrayList<Viewport>();
    TilingViewport tv;

    //FIXME slow with large files
    public BasePanel(Controller controller) {
	super();
	this.setLayout(new BorderLayout());
	// register view at the controller for the model change callback
	controller.addView(this);
	controller.getIoController().addView(this);
	controller.addView(this);
	final BasePanel bp = this;

    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent e) {

	if (e.getPropertyName().equals(RawImageReader.IMAGE_LOADED)) {
	    LOGGER.info("image loaded");
	    tv = new TilingViewport((RenderedImage) e.getNewValue());
	    // add component
	    //this.add((String) ((CR2ImageContainer) e.getOldValue()).getName(),
	    //    tv);
	    this.add(tv, BorderLayout.CENTER);
	    // creates and adds a viewport for the newly loaded image
	    //viewports.add(tv);
	    // set up the mutual listening on property changes
	    // viewports.get(viewports.size() - 1).addPropertyChangeListener(this);
	    // viewports.get(viewports.size() - 1).addPropertyChangeListener(bip);
	    tv.addPropertyChangeListener(this);

	    revalidate();
	    repaint();
	    firePropertyChange(STATE_UPDATE, 0, this);
	} else if (e.getPropertyName().equals(RawImageContainer.PROCESSED)) {
	    LOGGER.info("BasePanel processed");
	    tv.setOrigImg((RenderedImage) e.getNewValue());
	    firePropertyChange(STATE_UPDATE, 0, this);

	} else if (e.getPropertyName().equals(RawImageContainer.IMAGE_UPDATED)) {
	    LOGGER.info("BasePanel processed");
	    if (viewports.size() > 0) {
		//viewports.get(this.getSelectedIndex()).setOrigImg(
		//	(RenderedImage) e.getNewValue());
		//inform registered listeners about this changed state 
		//fireStateChanged();
	    }
	} else if (e.getPropertyName().equals(
		Controller.CONTAINER_REMOVED)) {
	    LOGGER.info("Removing viewport");
	    if (tv != null) {
		this.remove(tv);
		repaint();
	    }
	}
    }

    public Viewport getViewport() {
	return tv;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
		evt.getNewValue());

    }

}
