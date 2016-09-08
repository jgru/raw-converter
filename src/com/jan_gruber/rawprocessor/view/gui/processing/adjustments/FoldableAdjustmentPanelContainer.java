package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jan_gruber.rawprocessor.model.engine.store.CR2ImageContainer;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.view.gui.UpdatableComponent;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.mrlonee.swing.animation.AnimatedPanel;
import com.mrlonee.swing.animation.AnimatedPanel.AnimationType;
import com.spinn3r.log5j.Logger;

//inspired by http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels
public class FoldableAdjustmentPanelContainer implements UpdatableComponent {
    private static final Logger LOGGER = Logger.getLogger();

    private ArrayList<HeaderPanel> headers = new ArrayList<HeaderPanel>();
    private ArrayList<AdjustmentPanel> panels = new ArrayList<AdjustmentPanel>();
    private int counter;

    public void addPanel(AdjustmentPanel content) {
	addToParentContainer(content, content.getName());
    }

    public void addPanel(AdjustmentPanel content, String name) {
	addToParentContainer(content, name);
    }

    private void addToParentContainer(AdjustmentPanel p, String name) {
	headers.add(new HeaderPanel(counter, name));
	panels.add(p);
	counter++;
    }

    public JPanel getComponent() {
	JPanel panel = new JPanel(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.insets = new Insets(1, 3, 0, 3);
	gbc.weightx = 1.0;
	gbc.fill = gbc.HORIZONTAL;
	gbc.gridwidth = gbc.REMAINDER;
	for (int j = 0; j < headers.size(); j++) {
	    if (headers.get(j) != null && panels.get(j) != null) {
		panel.add(headers.get(j), gbc);
		panel.add(panels.get(j), gbc);
		panels.get(j).setVisible(false);
	    }
	}
	JLabel padding = new JLabel();
	gbc.weighty = 1.0;
	panel.add(padding, gbc);
	return panel;
    }

    private class HeaderPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private String name = "FoldablePanel";
	private boolean isExpanded;

	BufferedImage expandedIcon;
	BufferedImage foldedIcon;

	private int id;
	private JCheckBox cb;
	private int padding;

	public HeaderPanel(int id, String name) {
	    this.name = name;
	    this.id = id;
	    createImages();

	    this.setBorder(BorderFactory.createEtchedBorder());
	    this.setPreferredSize(new Dimension(325, expandedIcon.getHeight()));
	    this.setLayout(new BorderLayout());
	    this.setBorder(BorderFactory.createEtchedBorder());

	    JLabel headerLabel = new JLabel(name+" ");
	    headerLabel.setBorder(BorderFactory.createEmptyBorder(0,
		    foldedIcon.getWidth(), 0, 0));
	    this.add(headerLabel, BorderLayout.LINE_START);

	    cb = new JCheckBox();
	    cb.addChangeListener(new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
		    if (isExpanded != ((AbstractButton) e.getSource())
			    .isSelected()) {
			toggleFolding();
		    }
		}
	    });
	    cb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
		    foldedIcon.getWidth()));
	    this.add(cb, BorderLayout.LINE_END);
	    this.addMouseListener(new MouseHandler(this));
	    setRequestFocusEnabled(true);
	}

	public void toggleFolding() {
	    isExpanded = !isExpanded;
	    if (panels.get(id) != null) {
		panels.get(id).setVisible(isExpanded);
		panels.get(id).setActivated(isExpanded);
	    }
	    cb.setSelected(isExpanded);
	    this.getParent().validate();
	}

	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    int w = getWidth();
	    int h = getHeight();
	    if (isExpanded)
		g2.drawImage(expandedIcon, 0, 0, this);
	    else
		g2.drawImage(foldedIcon, 0, 0, this);
	}

	public boolean isExpanded() {
	    return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
	    this.isExpanded = isExpanded;
	}

	private void createImages() {

	    int w = 20;
	    int h = 25;
	    expandedIcon = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = expandedIcon.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setPaint(getBackground());
	    g2.fillRect(0, 0, w, h);
	    int[] x = { 2, w / 2, 18 };
	    int[] y = { 4, 15, 4 };
	    Polygon p = new Polygon(x, y, 3);
	    g2.setPaint(Color.DARK_GRAY);
	    g2.fill(p);
	    g2.setPaint(Color.DARK_GRAY);
	    g2.draw(p);
	    g2.dispose();

	    foldedIcon = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    g2 = foldedIcon.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setPaint(getBackground());
	    g2.fillRect(0, 0, w, h);

	    p = new Polygon(y, x, 3);
	    g2.setPaint(Color.DARK_GRAY);
	    g2.fill(p);
	    g2.setPaint(Color.DARK_GRAY);

	    g2.draw(p);
	    g2.dispose();
	}
    }

    private class MouseHandler extends MouseAdapter {
	private HeaderPanel hp;

	public MouseHandler(HeaderPanel headerPanel) {
	    hp = headerPanel;
	}

	public void mousePressed(MouseEvent e) {
	    hp.toggleFolding();
	}

    }

    public void foldAllPanels() {
	for (HeaderPanel p : headers) {
	    if (p != null && p.isExpanded()) {
		p.toggleFolding();
	    }
	}
    }

    @Override
    public void updateComponent(PropertyChangeEvent e) {
	if (e.getPropertyName().equals(BasePanel.STATE_UPDATE))
	    foldAllPanels();
    }

}
