package com.jan_gruber.rawprocessor.model.engine.store;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;
import javax.xml.transform.TransformerException;

import com.jan_gruber.rawprocessor.model.engine.io.IFD;
import com.spinn3r.log5j.Logger;

public class CR2ImageContainer extends RawImageContainer {
    private static final Logger LOGGER = Logger.getLogger();

    protected File mFile;
    protected ArrayList<IFD> ifdList;
    short[] data;

    public CR2ImageContainer(File file, ArrayList<IFD> ifdList,
	    BufferedImage[] thumbnails, CR2Metadata metadata,
	    final WritableRaster rawData) {
	this.mFile = file;
	this.fileName = file.getName();
	this.ifdList = ifdList;
	this.thumbnails = thumbnails;
	this.metadata = metadata;
	this.rawData = rawData;
	this.colorSpace = ColorSpace.TYPE_RGB;

	DataBufferUShort db = (DataBufferUShort) rawData.getDataBuffer();

	data = db.getData();
	imgToDisplay = createImageFromRasterData(rawData);

    }

    @Override
    public String getName() {
	return mFile.getName();
    }

    @Override
    public BufferedImage getThumbnail() {
	return thumbnails[0];
    }

    @Override
    public WritableRaster getRawData() {
	return rawData;
    }

    @Override
    public short[] getRawBuffer() {
	return data;
    }

    @Override
    public short[] getProcessedBuffer() {
	if (processedData != null) {
	    DataBufferUShort db = (DataBufferUShort) processedData
		    .getDataBuffer();
	    short[] data = db.getData();
	    return data;
	}
	return getRawBuffer();
    }

    @Override
    public String getMetadataAsXML() throws TransformerException, IOException {
	return ((TIFFMetadata) metadata).getMetadataAsTextXML();
    }

    public PlanarImage getImgToDisplay() {

	if (isProcessed && processedData != null)
	    return createImageFromRasterData(processedData);
	else
	    return imgToDisplay;
    }

    private final int SIXTEEN_BPC = 16;
    private final int RGB = 3;

    private PlanarImage createImageFromRasterData(WritableRaster raster) {
	int numBands = raster.getNumBands();
	BufferedImage bufferedImage = null;
	if (!isProcessed && numBands == 1) {
	    final ColorSpace colorSpace = ColorSpace
		    .getInstance(ColorSpace.CS_GRAY);
	    final ColorModel colorModel = new ComponentColorModel(colorSpace,
		    false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
	    final Properties properties = new Properties();

	    bufferedImage = new BufferedImage(colorModel, raster, false,
		    properties);
	} else if (numBands == RGB) {
	    DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	    WritableRaster outRaster = raster.createCompatibleWritableRaster(
		    raster.getWidth(), raster.getHeight());
	    DataBufferUShort outDb = (DataBufferUShort) outRaster
		    .getDataBuffer();
	    //create a actual copy of the data
	    System.arraycopy(db.getData(), 0, outDb.getData(), 0,
		    db.getData().length);

	    final ColorSpace outColorSpace;

	    if (this.colorSpace == ColorSpace.TYPE_RGB
		    || this.colorSpace == ColorSpace.CS_LINEAR_RGB)
		outColorSpace = ColorSpace
			.getInstance(ColorSpace.CS_LINEAR_RGB);
	    else {
		outColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	    }

	    final ColorModel colorModel = new ComponentColorModel(
		    outColorSpace, new int[] { SIXTEEN_BPC, SIXTEEN_BPC,
			    SIXTEEN_BPC }, false, false, Transparency.OPAQUE,
		    DataBuffer.TYPE_USHORT);
	    bufferedImage = new BufferedImage(colorModel, outRaster, false,
		    null);
	}
	TiledImage tiledImage = new TiledImage(bufferedImage, true);

	return tiledImage;
    }

    public void setImgToDisplay(PlanarImage imgToDisplay) {
	this.imgToDisplay = imgToDisplay;
    }

    @Override
    public byte[] getCfaPattern() {
	return this.metadata.getCfaPattern();
    }

    private boolean hasDetectedFeatures;
    private int[] featureCoords;

    @Override
    public boolean hasDetectedFeatures() {
	return hasDetectedFeatures;
    }

    @Override
    public void setHasDetectedFeatures(boolean hasDetectedFeatures) {
	this.hasDetectedFeatures = hasDetectedFeatures;
    }

    @Override
    public int[] getFeatureCoords() {
	return featureCoords;
    }

    @Override
    public void setFeatureCoords(int[] coords) {
	this.featureCoords = coords;
	if (coords != null)
	    hasDetectedFeatures = true;
	else
	    hasDetectedFeatures = false;
    }

    @Override
    public int getRasterWidth() {
	return rawData.getWidth();
	//return metadata.getWidth();
    }

    @Override
    public int getRasterHeight() {
	return rawData.getHeight();
	//return metadata.getHeight();
    }

    @Override
    public int getNumBands() {
	if (rawData != null)
	    return rawData.getNumBands();
	else
	    return 3;
    }

    @Override
    public short getOrientation() {
	return metadata.getOrientation();
    }

}
