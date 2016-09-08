package com.jan_gruber.rawprocessor.controller;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.jan_gruber.rawprocessor.view.AbstractComponent;
import com.spinn3r.log5j.Logger;

/**
 * @author JanGruber
 *
 */
public abstract class AbstractController implements PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger();
    protected PropertyChangeSupport propertyChangeSupport;
    protected ArrayList<AbstractComponent> registeredViews = new ArrayList<AbstractComponent>();

    public AbstractController() {
	propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addView(AbstractComponent view) {
	registeredViews.add(view);
    }

    public void removeView(AbstractComponent view) {
	registeredViews.remove(view);
    }

    //  Use this to observe property changes from registered models
    //  and propagate them on to all the views.
    public void propertyChange(final PropertyChangeEvent evt) {
	//update UI only from EventQueue
	if (!EventQueue.isDispatchThread()) {
	    try {
		EventQueue.invokeAndWait(new Runnable() {
		    @Override
		    public void run() {
			for (AbstractComponent view : registeredViews) {
			    view.modelPropertyChange(evt);
			    LOGGER.info(view.toString());
			}
		    }
		});
	    } catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}else{
	    for (AbstractComponent view : registeredViews) {
		    view.modelPropertyChange(evt);
		   LOGGER.info(view.toString());
		}
	}
    }

}
