package com.jan_gruber.rawprocessor.controller;

import java.beans.PropertyChangeEvent;

import javax.swing.Action;
import javax.swing.JFrame;

import com.jan_gruber.rawprocessor.controller.actions.io.DisplayMetadataAction;
import com.jan_gruber.rawprocessor.controller.actions.io.ExportAction;
import com.jan_gruber.rawprocessor.controller.actions.io.FileOpenAction;
import com.jan_gruber.rawprocessor.model.engine.io.RawImageWriter;
import com.jan_gruber.rawprocessor.model.engine.io.WriteParams;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;

public class IOController extends SubController {

    public IOController(Controller master) {
	super(master);
    }

    public Action getFileOpenAction(MainFrame f) {
	// creates the model
	return new FileOpenAction(this, f);
    }

    public Action getDisplayMetadataAction(JFrame f) {
	return new DisplayMetadataAction(this, f);
    }

    public Action getExportAction(JFrame f) {
	return new ExportAction(this, f);
    }

    // TODO add the other IO actions

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	System.err.println("Trigger" + evt.getPropertyName());
	//add the raw image container to the controller, to keep a reference on the model
	master.setImageContainer((RawImageContainer) evt.getOldValue());
	//propagiate the IMAGE_LOADED
	master.propertyChange(evt); 
    }

    public RawImageContainer getImageContainer() {
	return master.getImageContainer();
    }

    public void setImageContainer(RawImageContainer mImageContainer) {
	master.setImageContainer(mImageContainer);

    }

    public void update() {
	master.getImageContainer().setIsProcessed(false);

    }

    public void exportImage(WriteParams mParams) {
	RawImageWriter mWriter = new RawImageWriter(master.getImageContainer(),
		mParams);
	mWriter.export();
    }

}