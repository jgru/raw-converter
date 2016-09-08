package com.jan_gruber.rawprocessor.controller;

public class MasterController {
    private ProcessorController processorController;
    private PanoramaController panController;
    
    public MasterController(){
	initControllers();
	
    }
    private void initControllers() {
  	processorController = new ProcessorController();
  	panController= new PanoramaController();
  	// more to come
      }
    public ProcessorController getProcessorController() {
        return processorController;
    }
    public void setProcessorController(ProcessorController processorController) {
        this.processorController = processorController;
    }
    public PanoramaController getPanController() {
        return panController;
    }
    public void setPanController(PanoramaController panController) {
        this.panController = panController;
    }

    
}
