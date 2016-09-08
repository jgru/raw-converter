package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import com.mrlonee.swing.animation.AnimatedPanel;
import com.mrlonee.swing.animation.AnimatedPanel.AnimationType;

public abstract class AdjustmentPanel extends AnimatedPanel implements
	ActivatableComponent {
    boolean isActivated;
    String opName;

    public AdjustmentPanel() {
	this.setAnimationType(AnimationType.SlideAnimationFromTop);
	this.setTransparencyOnAnimation(true);
    }

    public String getOpName() {
	return opName;
    }

    @Override
    public void setActivated(boolean isActivated) {
	this.isActivated = isActivated;

    }

    @Override
    public boolean isActivated() {
	return this.isActivated;
    }
}
