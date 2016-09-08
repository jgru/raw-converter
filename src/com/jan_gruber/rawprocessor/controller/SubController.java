package com.jan_gruber.rawprocessor.controller;

public abstract class SubController extends AbstractController{
	protected Controller master;

	public SubController(Controller master) {
		this.master = master;
	}

	public Controller getMaster() {
		return master;
	}

	public void setMaster(Controller master) {
		this.master = master;
	}
}
