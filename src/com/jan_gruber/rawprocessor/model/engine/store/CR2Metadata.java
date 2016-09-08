package com.jan_gruber.rawprocessor.model.engine.store;

import java.util.ArrayList;
import java.util.Arrays;

import com.jan_gruber.rawprocessor.model.engine.io.IFD;
import com.jan_gruber.rawprocessor.model.engine.processor.operations.MatrixLookUp;
import com.spinn3r.log5j.Logger;

public class CR2Metadata extends TIFFMetadata implements RawMetadata {
    private static final Logger LOGGER = Logger.getLogger();
    // key information for processing the cr2 file
    // http://lclevy.free.fr/cr2/#key_info

    // from IFD#0
    protected int width;
    protected int height;
    protected String make;
    protected String model;
    protected short orientation;
    //from Makernote 
    protected short[] sensorInfo;
    protected short[] whiteBalanceRGGBLevels;
    protected short[] blackLevelsPerChannel;
    protected short whiteLevel;
    // from IFD#3
    protected long stripOffset;
    protected long stripByteCount;
    protected short[] cr2slices;
    // TODO retrieve info from metadata
    protected byte[] cfaPattern = { 0, 1, 1, 2 }; // RGGB

    public CR2Metadata(ArrayList<IFD> ifdList) {
	super(ifdList);
	prepareKeyInformation();
    }

    private void prepareKeyInformation() {
	width = (Short) ifdList.get(0).getTag(0x100).getValue();
	height = (Short) ifdList.get(0).getTag(0x101).getValue();
	make = (String) ifdList.get(0).getTag(0x10F).getValue();
	model = (String) ifdList.get(0).getTag(0x110).getValue();
	orientation= (Short) ifdList.get(0).getTag(0x112).getValue();
	whiteBalanceRGGBLevels = parseWhiteBalanceTag(ifdList.get(0)
		.getSubDir().getSubDir().getTag(0x4001));

	blackLevelsPerChannel = getBlackLevelsPerChannel(ifdList.get(0)
		.getSubDir().getSubDir().getTag(0x4001));

	whiteLevel = getWhiteLevel(ifdList.get(0).getSubDir().getSubDir()
		.getTag(0x4001));

	sensorInfo = getSensorInfo(ifdList.get(0).getSubDir().getSubDir()
		.getTag(0x00e0));

	stripOffset = (Integer) ifdList.get(3).getTag(0x111).getValue();
	stripByteCount = (Integer) ifdList.get(3).getTag(0x117).getValue();

	if (ifdList.get(3).getTag(0xc640) != null)
	    cr2slices = (short[]) ifdList.get(3).getTag(0xc640).getValue();

	LOGGER.info("Successfully prepared key information");
    }

    //detailled info about sensorInfo Tags
    //http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Canon.html#SensorInfo
    //0: nn 1: 5920 w  2: 3950 h   3: 1 left border  4: 1 right border  5: 140 black mask left border   
    //6: 96 black mask top  7: 5899 black mask right  8: 3935 black mask bottom
    private short[] getSensorInfo(TIFFField tag) {
	if (tag.getCode() != 0x00e0)
	    throw new IllegalArgumentException(
		    "No sensorInfo Tag was handed over");
	short[] dataFromTag = (short[]) tag.getValue();

	return dataFromTag;

    }

    //detailed information about the ColorData Tags: http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Canon.html#ColorData1

    //src: http://lclevy.free.fr/cr2/#key_info  #5.1 White balance values in the CR2 file
    /* 582 is the length of the 0x4001 tag for 20D and 350D. skip length is 50 bytes. 
     *  (See Phil Harvey's "ColorBalance1" WB_RGGBLevelsAsShot tag at short offset 25)
     * 653 is the length for 1D Mark II and 1Ds Mark II. skip length is 68 bytes. 
     *  (See "ColorBalance2" WB_RGGBLevelsAsShot tag of Phil Harvey, short offset is 34.)
     * 5120 is the size for the Canon G10. skip offset is 142 bytes, 71 shorts.
     * default skip value is 126 bytes, 63 shorts. See "ColorBalance3" and "ColorBalance4" WB_RGGBLevelsAsShot tags             
     */

    private short[] getBlackLevelsPerChannel(TIFFField tag) {
	if (tag.getCode() != 0x4001)
	    throw new IllegalArgumentException(
		    "No ColorData Tag was handed over!");

	short[] colorData = ((short[]) tag.getValue());
	int offset = 0;
	switch (colorData.length) {
	//ColorData2
	case 653:
	    //no blacklevel information
	    return null;
	    //ColorData7
	case 1312:
	    offset = 504;
	    break;
	//ColorData4
	case 1250:
	    offset = 715;
	    break;
	//ColorData3
	case 796:
	    offset = 196;
	    break;
	default:
	    return null;

	    //FIXME add more
	}

	short[] blackLevelsPerChannel = new short[4];
	for (int i = 0; i < blackLevelsPerChannel.length; i++) {
	    blackLevelsPerChannel[i] = (short) (colorData[offset + i] & 0xffff);
	}

	LOGGER.info("BlackLevels: " + Arrays.toString(blackLevelsPerChannel));
	return blackLevelsPerChannel;
    }

    private short getWhiteLevel(TIFFField tag) {
	if (tag.getCode() != 0x4001) {
	    LOGGER.warn("No ColorData Tag was handed over!");
	    return (1 << 14) - 1;
	}
	short[] colorData = ((short[]) tag.getValue());
	int offset = 0;
	switch (colorData.length) {
	//ColorData7
	case 1312:
	    offset = 509;
	    break;
	case 1250:
	    //5d mkii
	    offset = 719;
	    break;
	default:
	    return ((1 << 14) - 1);
	    //FIXME add more
	}
	LOGGER.info("WhiteLevel: %d", colorData[offset]);

	return (colorData[offset]);

    }

    private short[] parseWhiteBalanceTag(TIFFField wbTag) {
	if (wbTag.getCode() != 0x4001)
	    throw new IllegalArgumentException(
		    "No ColorData Tag was handed over!");

	int offset = 0;
	//unsigned short data stored in the tag 0x4001
	short[] colorData = ((short[]) wbTag.getValue());
	LOGGER.info("Length of data in ColorData- Tag: %d", colorData.length);

	LOGGER.info("Data: " + Arrays.toString(colorData));
	switch (colorData.length) {
	case 582:
	    System.err.println("582");
	    offset = 50 / 2;
	    break;
	case 653:
	    System.err.println("653");
	    offset = 68 / 2;
	    break;
	case 5120:
	    System.err.println("5120");
	    offset = 142 / 2;
	    break;
	case 1250:
	    //5d mkii
	    offset = 126 / 2;
	    break;
	case 1312:
	    //5d mkiii
	    offset = 63;
	    break;
	default:
	    offset = 63;
	    break;
	//TODO => correct offset for 5d2 +3

	}
	//RGGB multipliers
	short[] whiteBalanceRGGBMultipliers = new short[28];
	int c = 0;
	int s = 0;
	for (int i = 0; c < whiteBalanceRGGBMultipliers.length; i++) {
	    //skip 1 byte after 4
	    if (s > 3) {
		s = 0;
		continue;
	    }
	    whiteBalanceRGGBMultipliers[c] = (short) (colorData[offset + i] & 0xffff);
	    c++;
	    s++;

	}

	LOGGER.info("WhiteBalance multipliers read: "
		+ Arrays.toString(whiteBalanceRGGBMultipliers));
	return whiteBalanceRGGBMultipliers;
    }

    @Override
    public int getWidth() {
	return width;
    }

    @Override
    public int getHeight() {
	return height;
    }

    public int getSensorWidth() {
	if (sensorInfo != null)
	    return sensorInfo[1];
	return width;
    }

    public int getSensorHeight() {
	if (sensorInfo != null)
	    return sensorInfo[2];
	return height;
    }

    public int getLeftCrop() {
	if (sensorInfo != null)
	    return sensorInfo[5];
	return 0;
    }

    public int getTopCrop() {
	if (sensorInfo != null)
	    return sensorInfo[6];
	return 0;
    }

    public int getRightCrop() {
	if (sensorInfo != null)
	    return sensorInfo[7];
	return 0;
    }

    public int getBottomCrop() {
	if (sensorInfo != null)
	    return sensorInfo[8];
	return 0;
    }

    public long getStripOffset() {
	return stripOffset;
    }

    public void setStripOffset(long stripOffset) {
	this.stripOffset = stripOffset;
    }

    public long getStripByteCount() {
	return stripByteCount;
    }

    @Override
    public byte[] getCfaPattern() {
	return cfaPattern;
    }

    public void setCfaPattern(byte[] cfaPattern) {
	this.cfaPattern = cfaPattern;
    }

    public short[] getCr2slices() {
	return cr2slices;
    }

    public void setCr2slices(short[] cr2slices) {
	this.cr2slices = cr2slices;
    }

    @Override
    public String getMake() {
	return make;
    }

    @Override
    public String getModel() {
	return model;
    }

    @Override
    public long getStripOffsetToRawData() {
	return stripOffset;
    }

    @Override
    public short[] getWhiteBalanceRGGBMultipliers() {
	return whiteBalanceRGGBLevels;
    }

    public void setBlackLevelsPerChannel(short[] blackLevelsPerChannel) {
	this.blackLevelsPerChannel = blackLevelsPerChannel;
    }

    @Override
    public short[] getBlackLevelsPerChannel() {
	return blackLevelsPerChannel;
    }

    @Override
    public short getWhiteLevel() {
	return whiteLevel;
    }

    @Override
    public double[] getColorMatrix1() {
	return MatrixLookUp.getMatrixByCameraName(model);
    }
    /**
     *
     *  AsShotNeutral specifies the selected white balance at time of capture, 
     *  encoded as the coordinates of a perfectly neutral color in linear reference 
     *  space values. The inclusion of this tag precludes the inclusion of the AsShotWhiteXY tag.
     * @return values, used by DNG-Tag 50728 (basically the 1/ each RGB Multiplier
     */
    @Override
    public float[] getNeutralWhiteBalanceMultipliers() {
	float[] rgbMultipliers = new float[3];

	for (int i = 0; i < rgbMultipliers.length; i++) {
	    if (i > 0)
		//compensate rGGb
		rgbMultipliers[i] = (float) (1.0/(whiteBalanceRGGBLevels[i + 1] / 1024.0));
	    else
		rgbMultipliers[i] =(float) (1.0/(whiteBalanceRGGBLevels[i] / 1024.0));
	}

	return rgbMultipliers;
    }

    @Override
    public short getOrientation() {
	return orientation;
    }

}
