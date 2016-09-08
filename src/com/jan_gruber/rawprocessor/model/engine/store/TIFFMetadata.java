package com.jan_gruber.rawprocessor.model.engine.store;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.jan_gruber.rawprocessor.model.engine.io.IFD;
import com.jan_gruber.rawprocessor.model.engine.io.TIFFTagParser;
import com.spinn3r.log5j.Logger;


public class TIFFMetadata extends IIOMetadata {
	private static final Logger LOGGER = Logger.getLogger();
	protected ArrayList<IFD> ifdList;
	private boolean isBuilt = false;
	private Node metadataTree;

	public TIFFMetadata(ArrayList<IFD> ifdList) {
		this.ifdList = ifdList;
	}

	public String getMetadataAsTextXML() throws TransformerException,
			IOException {
		return getNodeAsTextXML(getAsTree(""));
	}

	public String getEXIFAsTextXML() throws TransformerException, IOException {
		return getNodeAsTextXML(getEXIFNode());
	}

	private String getNodeAsTextXML(Node n) throws TransformerException,
			IOException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		Source source = new DOMSource(n);
		transformer.transform(source, result);
		writer.close();
		String xml = writer.toString();

		return xml;
	}

	public Node getEXIFNode() {
		Iterator<IFD> it = ifdList.iterator();
		IFD ifd = null;
		while (it.hasNext()) {
			ifd = (IFD) it.next();
			if (ifd.hasExifDir()) {
				return getIFDAsNode(ifd.getSubDir());
			}
		}
		
		LOGGER.warn("No EXIF directory found");
		return null;
	}

	@Override
	public Node getAsTree(String formatName) {
		if (isBuilt)
			return metadataTree;
		else if (!isBuilt && ifdList != null) {
			metadataTree = buildTree(ifdList);
			return metadataTree;
		} else {
			LOGGER.warn("No metadata node available!");
			return null;
		}
	}

	private Node buildTree(ArrayList<IFD> ifdList) {
		metadataTree = new IIOMetadataNode("TIFF");
		for (int i = 0; i < ifdList.size(); i++) {
			Node tmpIFDNode = getIFDAsNode(ifdList.get(i));
			metadataTree.appendChild(tmpIFDNode);
		}
		return metadataTree;
	}

	private Node getIFDAsNode(IFD ifd) {
		IIOMetadataNode ifdNode = new IIOMetadataNode("IFD");
		Set<Integer> keySet = (Set<Integer>) ifd.getTagList().keySet();
		Iterator it = keySet.iterator();
		while (it.hasNext()) {
			TIFFField tmpField = ifd.getTag(((Integer) it.next()).intValue());
			ifdNode.appendChild(getTIFFFieldAsNode(tmpField));
		}
		// append subdirectory if present
		if (ifd.hasSubDirectory()) {
			ifdNode.appendChild(getIFDAsNode(ifd.getSubDir()));
		}

		return ifdNode;
	}

	private Node getTIFFFieldAsNode(TIFFField tmpField) {
		IIOMetadataNode tagNode = new IIOMetadataNode("TIFFField");
		tagNode.setAttribute("Tag", Integer.toString(tmpField.getCode()));
		tagNode.setAttribute("Name", tmpField.getName());
		IIOMetadataNode formatNode = new IIOMetadataNode(""
				+ TIFFTagParser.typeLookUp[tmpField.getDataFormat()]);
		for (int i = 0; i < tmpField.getComponentCount(); i++) {
			switch (tmpField.getDataFormat()) {
			case TIFFTagParser.TYPE_LONG:
			case TIFFTagParser.TYPE_SIGNED_LONG:
			case TIFFTagParser.TYPE_SIGNED_SHORT:
			case TIFFTagParser.TYPE_SIGNED_BYTE:
			case TIFFTagParser.TYPE_USHORT:
			case TIFFTagParser.TYPE_BYTE:
				formatNode.setAttribute("value",
						Integer.toString(tmpField.intValue[i]));
				break;

			case TIFFTagParser.TYPE_RATIONAL:
			case TIFFTagParser.TYPE_SIGNED_RATIONAL:
			case TIFFTagParser.TYPE_FLOAT:
			case TIFFTagParser.TYPE_DOUBLE:
				formatNode.setAttribute("value", "rational");// tmpField.getRationalValues()[i].toString());
				break;

			case TIFFTagParser.TYPE_ASCII:
				final StringBuilder buffer = new StringBuilder();
				final String value = (String) tmpField.getValue();

				for (int j = 0; j < value.length(); j++) {
					char c = value.charAt(j);

					if ((c >= 32) && (c <= 127)) {
						buffer.append(c);
					}

					else {
						buffer.append("\\u0x" + Integer.toHexString(c));
					}
				}

				formatNode.setAttribute("value", buffer.toString());
				i = (int) tmpField.getComponentCount();

				break;
			}
		}
		tagNode.appendChild(formatNode);
		return tagNode;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		throw new UnsupportedOperationException("CR2Metadata.mergeTree()");

	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException("CR2Metadata.reset()");

	}

	public ArrayList<IFD> getIfdList() {
		return ifdList;
	}

	public void setIfdList(ArrayList<IFD> ifdList) {
		this.ifdList = ifdList;
	}

}
