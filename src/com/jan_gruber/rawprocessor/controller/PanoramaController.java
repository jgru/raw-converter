package com.jan_gruber.rawprocessor.controller;

import java.beans.PropertyChangeEvent;

import javax.swing.Action;

import com.jan_gruber.rawprocessor.controller.actions.panorama.FeatureDetectionAction;
import com.jan_gruber.rawprocessor.controller.actions.panorama.StitchingAction;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaParameters;
import com.jan_gruber.rawprocessor.model.engine.panorama.PanoramaEditor;
import com.jan_gruber.rawprocessor.view.gui.MainFrame;
import com.jan_gruber.rawprocessor.view.gui.panorama.PanoramaView;
import com.jan_gruber.rawprocessor.view.gui.panorama.ParamPanel;
import com.spinn3r.log5j.Logger;

public class PanoramaController extends Controller {
    private static final Logger LOGGER = Logger.getLogger();
    protected PanoramaEditor mEditor;

    public PanoramaController() {	
	super();
	mEditor = new PanoramaEditor();
	mEditor.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	LOGGER.info("Panorama property change");
	if(evt.getPropertyName().equals(PanoramaEditor.STITCHING_UPDATE)){
	   while(this.getImageCount()>0){
	       this.removeLastAddedImageContainer();
	   }
	}
	
	//propagiate to registered views
	super.propertyChange(evt);
	
    }

    public Action getFeatureDetectionAction(MainFrame f, ParamPanel pv) {
	return new FeatureDetectionAction(this, f, pv);
    }
    public Action getStichingAction(MainFrame f, ParamPanel paramPanel) {
	return new StitchingAction(this, f, paramPanel);
    }

    public void startFeatureDetection(PanoramaParameters fdp) {
	mEditor.detectFeatures(mContainers, fdp);

    }

    public void startStitching(PanoramaParameters fdp, String outPath) {
	mEditor.stitchImages(mContainers, fdp, outPath);
    }

    public void update() {
	mContainers.get(mContainers.size() - 1).setIsProcessed(false);

    }

}
