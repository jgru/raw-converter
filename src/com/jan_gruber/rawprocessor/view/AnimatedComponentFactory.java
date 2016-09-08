package com.jan_gruber.rawprocessor.view;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.mrlonee.swing.animation.AnimatedPanel;
import com.mrlonee.swing.animation.AnimatedPanel.AnimationType;

public class AnimatedComponentFactory {
    public static AnimatedPanel createAnimatedPanel(JPanel content) {
	AnimatedPanel ap = new AnimatedPanel();
	ap.add(content);
	ap.setAnimationType(AnimationType.SlideAnimationFromTop);
	ap.setTransparencyOnAnimation(true);

	return ap;
    }
    public static AnimatedPanel createAnimatedPanel(JComponent content) {
	AnimatedPanel ap = new AnimatedPanel();
	ap.add(content);
	ap.setLayout(new BoxLayout(ap, BoxLayout.X_AXIS));
	ap.setPreferredSize(content.getPreferredSize());
	ap.setTransparencyOnAnimation(true);
	
	return ap;
    }
}
