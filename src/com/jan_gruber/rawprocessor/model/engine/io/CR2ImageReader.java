package com.jan_gruber.rawprocessor.model.engine.io;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.TransformerException;

import com.jan_gruber.rawprocessor.controller.IOController;
import com.jan_gruber.rawprocessor.model.engine.store.CR2ImageContainer;
import com.jan_gruber.rawprocessor.model.engine.store.CR2Metadata;
import com.spinn3r.log5j.Logger;

public class CR2ImageReader extends RawImageReader {
    private static final Logger LOGGER = Logger.getLogger();
    // the raw file to read
    protected File mFile;
    // stream of bytes
    protected RAWImageInputStreamImpl mStream;
    protected CR2Parser mParser;

    public CR2ImageReader(File mFile) {
	this.mFile = mFile;
    }

    public void readFile(boolean cropMaskedPixels) {
	if (cropMaskedPixels)
	    readCroppedCR2File();
	else
	    readCompleteCR2File();

    }

    private CR2ImageContainer readCompleteCR2File() {
	try {
	    mStream = initReader(mFile);
	    mParser = new CR2Parser(mStream);

	    mParser.setByteOrder(mStream, mParser.checkHeader(mStream));
	    if (mParser.checkCR2(mStream)) {
		ArrayList<IFD> ifdList = mParser.retrieveIFDs(mStream);
		CR2Metadata metadata = new CR2Metadata(ifdList);

		final WritableRaster rawData = mParser.parseRawData(mStream,
			metadata,false);
		final CR2ImageContainer mRawImageContainer = new CR2ImageContainer(
			mFile, ifdList, null, metadata, rawData);

		dispatchPropertyChangeEvents(mRawImageContainer);

		return mRawImageContainer;
	    } else
		LOGGER.warn("No valid CR2 file");

	} catch (FileNotFoundException e) {
	    LOGGER.warn(e);
	    e.printStackTrace();
	} catch (IOException e) {
	    LOGGER.warn(e);
	    e.printStackTrace();
	}
	return null;
    }

    private CR2ImageContainer readCroppedCR2File() {
	try {
	    mStream = initReader(mFile);
	    mParser = new CR2Parser(mStream);

	    mParser.setByteOrder(mStream, mParser.checkHeader(mStream));
	    if (mParser.checkCR2(mStream)) {
		ArrayList<IFD> ifdList = mParser.retrieveIFDs(mStream);
		//BufferedImage[] thumbnails = mParser.retrieveThumbnails(
		//	mStream, ifdList);
		CR2Metadata metadata = new CR2Metadata(ifdList);

		WritableRaster rawData = mParser
			.parseRawData(mStream, metadata, true);
		CR2ImageContainer mRawImageContainer = new CR2ImageContainer(
			mFile, ifdList, null, metadata, rawData);

		dispatchPropertyChangeEvents(mRawImageContainer);

		return mRawImageContainer;
	    } else
		LOGGER.warn("No valid CR2 file");

	} catch (FileNotFoundException e) {
	    LOGGER.warn(e);
	    e.printStackTrace();
	} catch (IOException e) {
	    LOGGER.warn(e);
	    e.printStackTrace();
	}
	return null;
    }

    private RAWImageInputStreamImpl initReader(File f)
	    throws FileNotFoundException, IOException {
	FileImageInputStream fileImageStream = null;
	fileImageStream = new FileImageInputStream(f);
	RAWImageInputStreamImpl rawImageStream = new RAWImageInputStreamImpl(
		fileImageStream);
	rawImageStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
	return rawImageStream;

    }

    public File getFile() {
	return mFile;
    }

    public void setFile(File mFile) {
	this.mFile = mFile;
    }

    private void dispatchPropertyChangeEvents(
	    CR2ImageContainer mRawImageContainer) {
	// inform about the changed state
	propertyChangeSupport.firePropertyChange(IMAGE_LOADED,
		mRawImageContainer, mRawImageContainer.getImgToDisplay());
    }

}
