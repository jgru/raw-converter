package com.jan_gruber.rawprocessor.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.Action;

import com.jan_gruber.rawprocessor.controller.actions.PanoramaViewAction;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.spinn3r.log5j.Logger;

public class Controller extends AbstractController {
    private static final Logger LOGGER = Logger.getLogger();
    private IOController ioController;

    public static final String CONTAINER_REMOVED = "CONTAINER_REMOVED";

    // reference on the model
    protected ArrayList<RawImageContainer> mContainers = new ArrayList<RawImageContainer>();
    int index;

    public Controller() {
	initIOControllers();

    }

    private void initIOControllers() {
	ioController = new IOController(this);
	// more to come
    }

    public Action getPanoramaViewAction(MainFrame f) {
	return new PanoramaViewAction(this, f);
    }

    public IOController getIoController() {
	return ioController;
    }

    public void setIoController(IOController ioController) {
	this.ioController = ioController;
    }

    public boolean contains(String fileName) {
	for (RawImageContainer con : mContainers) {
	    if (con.getName().equals(fileName))
		return true;
	}

	return false;
    }

    public RawImageContainer getImageContainer() {
	if (mContainers.size() > 0)
	    return mContainers.get(index);
	else
	    return null;
    }

    public void setImageContainer(RawImageContainer mImageContainer) {
	this.mContainers.add(mImageContainer);
	mImageContainer.addPropertyChangeListener(this);
    }

    public void removeLastAddedImageContainer() {
	if (mContainers.size() > 0) {
	    index = this.mContainers.size() - 1;
	    propertyChange(new PropertyChangeEvent(mContainers.get(index),
		    CONTAINER_REMOVED, -1, 5));
	    this.mContainers.remove(index);
	    index = this.mContainers.size() - 1;
	}
    }
    public void removeImageContainer(int i) {
	if (mContainers.size() > i) {
	    propertyChange(new PropertyChangeEvent(mContainers.get(i),
		    CONTAINER_REMOVED, -1, 5));
	    this.mContainers.remove(index);
	    index = this.mContainers.size() - 1;
	}
    }
    
    public int getImageCount(){
	LOGGER.info("Size: "+ mContainers.size());
	return mContainers.size();	
    }
}
