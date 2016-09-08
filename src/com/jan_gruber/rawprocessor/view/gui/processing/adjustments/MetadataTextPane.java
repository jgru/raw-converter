package com.jan_gruber.rawprocessor.view.gui.processing.adjustments;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.xml.transform.TransformerException;

import xmleditorkit.XMLEditorKit;

import com.jan_gruber.rawprocessor.controller.IOController;
import com.jan_gruber.rawprocessor.model.engine.io.RawImageReader;
import com.jan_gruber.rawprocessor.view.AbstractComponent;
import com.jan_gruber.rawprocessor.view.gui.processing.viewer.BasePanel;
import com.spinn3r.log5j.Logger;

/**
 * @author JanGruber
 * 
 */
public class MetadataTextPane extends JTextPane implements AbstractComponent {
	private static final Logger LOGGER = Logger.getLogger();
	private HashMap<Integer, String> metadataMap = new HashMap<Integer, String>();
	int counter = 0;

	// uses external jar for xml formatting
	// http://java-sl.com/xml_editor_kit.html
	public MetadataTextPane(IOController ioController) throws TransformerException, IOException, NullPointerException {
		super();
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setPreferredSize(new Dimension(256, 100));
		this.setEditorKit(new XMLEditorKit());

		ioController.addView(this);
		metadataMap.put(counter++,ioController.getImageContainer().getMetadataAsXML());
		this.setText(metadataMap.get(0));
	}

	/**
	 * called by MainFrame, which acts as a StateChangedListener for BasePanel
	 * (the tabbed pane, which presents the images).
	 * 
	 * @param bp
	 */
	public void update(BasePanel bp) {
		int index = 0;//bp.getSelectedIndex();
		this.setText(metadataMap.get(Integer.valueOf(index)));
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(RawImageReader.METADATA_LOADED)) {
			metadataMap.put(counter, (String) e.getNewValue());
		}
		this.setText(metadataMap.get(counter));
		counter++;
	}

	public static SimpleAttributeSet BRACKET_ATTRIBUTES = new SimpleAttributeSet();
	public static SimpleAttributeSet TAGNAME_ATTRIBUTES = new SimpleAttributeSet();
	public static SimpleAttributeSet ATTRIBUTENAME_ATTRIBUTES = new SimpleAttributeSet();
	public static SimpleAttributeSet ATTRIBUTEVALUE_ATTRIBUTES = new SimpleAttributeSet();
	public static SimpleAttributeSet PLAIN_ATTRIBUTES = new SimpleAttributeSet();
	public static SimpleAttributeSet COMMENT_ATTRIBUTES = new SimpleAttributeSet();
	static {
		StyleConstants.setBold(TAGNAME_ATTRIBUTES, true);
		StyleConstants.setForeground(TAGNAME_ATTRIBUTES, Color.GREEN.darker());

		StyleConstants.setBold(ATTRIBUTENAME_ATTRIBUTES, true);

		StyleConstants.setItalic(ATTRIBUTEVALUE_ATTRIBUTES, true);
		StyleConstants.setForeground(ATTRIBUTEVALUE_ATTRIBUTES, Color.BLUE);

		StyleConstants.setFontSize(PLAIN_ATTRIBUTES,
				StyleConstants.getFontSize(PLAIN_ATTRIBUTES) - 5);
		StyleConstants.setForeground(PLAIN_ATTRIBUTES, Color.DARK_GRAY);

		StyleConstants.setFontSize(COMMENT_ATTRIBUTES,
				StyleConstants.getFontSize(COMMENT_ATTRIBUTES) - 5);
		StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.GRAY);
		StyleConstants.setItalic(COMMENT_ATTRIBUTES, true);
	}

}
