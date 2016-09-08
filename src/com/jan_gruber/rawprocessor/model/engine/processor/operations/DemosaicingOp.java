package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Arrays;

import com.jan_gruber.rawprocessor.model.engine.store.CR2ImageContainer;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.spinn3r.log5j.Logger;
import com.sun.media.jai.rmi.SampleModelProxy;

public class DemosaicingOp extends PipelineComponent {
    private static final Logger LOGGER = Logger.getLogger();
    public static final int TYPE_NEAREST_NEIGHBOUR = 0;
    public static final int TYPE_BILINEAR = 1;
    public static final int TYPE_BICUBIC = 2;
    public static final int TYPE_HUE_INTERPOLATION = 3;
    public static final int TYPE_ADAPTIVE_INTERPOLATION = 4;

    private int type;

    public DemosaicingOp(int type) {
	super();
	this.rank = 2;
	this.type = type;

    }

    @Override
    public void apply(RawImageContainer mRaw) {
	// compute offsets etc....
	computeOffsets(mRaw.getProcessedData(), mRaw.getCfaPattern());

	// kickoff demosaicing algorithm
	switch (type) {
	case TYPE_NEAREST_NEIGHBOUR:
	    demosaicNearestNeighbor(mRaw.getProcessedData());
	    break;
	case TYPE_BILINEAR:
	    LOGGER.info("bilinear demosaicing");
	    demosaicBilinear(mRaw.getProcessedData());
	    break;
	case 2:
	    demosaicConstantBasedHue(mRaw.getProcessedData());
	    break;
	case 3:
	    // TODO
	    break;
	default:
	    // should never happen
	    demosaicBilinear(mRaw.getProcessedData());
	    break;
	}
    }

    int pixelStride;
    int scanline;

    final int redOffset = 0;
    final int greenOffset = 1;
    final int blueOffset = 2;

    int left;
    int right;
    int topLeft;
    int topRight;
    int bottomLeft;
    int bottomRight;
    int bottom;
    int top;

    private void computeOffsets(WritableRaster rgbRaster, byte[] cfaPattern) {
	pixelStride = rgbRaster.getNumBands();
	scanline = pixelStride * rgbRaster.getWidth();
	left = -pixelStride;
	topLeft = -scanline - pixelStride;
	top = -scanline;
	topRight = -scanline + pixelStride;
	right = pixelStride;
	bottomLeft = scanline - pixelStride;
	bottom = scanline;
	bottomRight = scanline + pixelStride;

    }

    int brink = 2;

    private static final int[] GREEN_I_KERNEL = { 0, 1, 0, 1, 4, 1, 0, 1, 0 };

    /*
     * Visualized RGGB pattern
    *			x	x+1	x+2	x+3
    *	___________________________________________		
    *	y	|	R	G	R	G	
    *		|
    *	y+1 	|	G	B	G	B
    *		|
    *	y+2	|	R	G	R	G	
    *		|
    *	y+3	|	G	B	G	B
    *
    */

    private void demosaicConstantBasedHue(WritableRaster raster) {
	LOGGER.info("start constant hue- based demosaicing");
	int width = raster.getWidth();
	int height = raster.getHeight();
	int pixelStride = raster.getNumBands();
	int scanline = width * pixelStride;
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	short[] pixelData = db.getData();
	long startTime = System.currentTimeMillis();

	//interpolate green plane

	for (int k = brink; k < height - brink; k += 1) {
	    for (int l = brink * pixelStride; l < scanline
		    - (brink * pixelStride); l += pixelStride) {
		int scanPos = l + k * scanline;
		int tmpG = db.getElem(scanPos + greenOffset);

		if (tmpG == 0) {
		    //inteprolate g 
		    int iG = (db.getElem(scanPos + top + greenOffset)
			    + db.getElem(scanPos + bottom + greenOffset)
			    + db.getElem(scanPos + left + greenOffset) + db
			    .getElem(scanPos + right + greenOffset)) / 4;

		    iG = iG > 0xffff ? 0xffff : iG < 0 ? 0 : iG;
		    db.setElem(scanPos + greenOffset, iG);
		}

	    }
	}
	long endTime = System.currentTimeMillis();
	System.err.println("duration: " + (endTime - startTime));

	float beta = 64f;
	for (int y = brink; y < height - brink; y += 1) {
	    for (int x = brink * pixelStride; x < scanline
		    - (brink * pixelStride); x += pixelStride) {
		int scanPos = x + y * scanline;

		int tmpR = db.getElem(scanPos);
		int tmpG = db.getElem(scanPos + greenOffset);
		int tmpB = db.getElem(scanPos + blueOffset);

		if (tmpR == 0 && tmpB != 0) {
		    //blue pixel
		    double hueR = 0;
		    int posOffset = 0;
		    for (int v = -1; v < 2; v += 2) {
			for (int u = -1; u < 2; u += 2) {
			    posOffset = (u * pixelStride) + (v * scanline);
			    hueR += ((db.getElem(scanPos + posOffset) + beta) / (db
				    .getElem(scanPos + posOffset + greenOffset) + beta));
			}
		    }

		    int fillR = (int) (tmpG * hueR / 4);
		    fillR = fillR > 0xffff ? 0xffff : fillR < 0 ? 0 : fillR;
		    //fill up 
		    db.setElem(scanPos, (short) fillR);
		} else if (tmpB == 0 && tmpR != 0) {
		    //red pixel
		    double hueB = 0;
		    int posOffset = 0;
		    for (int u = -1; u < 2; u += 2) {
			for (int v = -1; v < 2; v += 2) {
			    posOffset = (u * pixelStride) + (v * scanline);
			    hueB += ((db.getElem(scanPos + posOffset
				    + blueOffset) + beta) / (db.getElem(scanPos
				    + posOffset + greenOffset) + beta));
			}
		    }

		    int fillB = (int) (tmpG * hueB / 4);
		    fillB = fillB > 0xffff ? 0xffff : fillB < 0 ? 0 : fillB;
		    //fill up b
		    db.setElem(scanPos + blueOffset, (short) fillB);
		} else if (tmpR == 0 && tmpB == 0) {
		    //green pixel
		    double hueR;
		    double hueB;
		    if (db.getElem(scanPos + right) == 0) {
			//green in blue row (because neighboring red is 0)
			hueR = ((db.getElem(scanPos + top) + beta) / (db
				.getElem(scanPos + top + greenOffset) + beta))
				+ ((db.getElem(scanPos + bottom) + beta) / (db
					.getElem(scanPos + bottom + greenOffset) + beta));
			hueB = ((db.getElem(scanPos + left + blueOffset) + beta) / (db
				.getElem(scanPos + left + greenOffset) + beta))
				+ ((db.getElem(scanPos + right + blueOffset) + beta) / (db
					.getElem(scanPos + right + greenOffset) + beta));
		    } else {
			//green in red row
			hueB = ((db.getElem(scanPos + top + blueOffset) + beta) / (db
				.getElem(scanPos + top + greenOffset) + beta))
				+ ((db.getElem(scanPos + bottom + blueOffset) + beta) / (db
					.getElem(scanPos + bottom + greenOffset) + beta));
			hueR = ((db.getElem(scanPos + left) + beta) / (db
				.getElem(scanPos + left + greenOffset) + beta))
				+ ((db.getElem(scanPos + right) + beta) / (db
					.getElem(scanPos + right + greenOffset) + beta));
		    }
		    int fillR = (int) (tmpG * hueR / 2);
		    int fillB = (int) (tmpG * hueB / 2);
		    fillR = fillR > 0xffff ? 0xffff : fillR < 0 ? 0 : fillR;
		    fillB = fillB > 0xffff ? 0xffff : fillB < 0 ? 0 : fillB;

		    //fill up 
		    db.setElem(scanPos, (short) fillR);
		    db.setElem(scanPos + blueOffset, (short) fillB);

		}
	    }
	}

    }

    private void interpolateColorPlane(short[] pixelData, int width,
	    int height, int pixelStride, int[] kernel, int greenOffset) {
	//FIXME-> factor 100 slower than simple iteration and interpolation with one databuffer

	short[] output = new short[pixelData.length];
	System.arraycopy(pixelData, 0, output, 0, pixelData.length);
	int kernelWidth = kernel.length / 3;
	int radius = kernelWidth / 2;

	for (int y = brink; y < height; y++) {
	    for (int x = brink * pixelStride; x < scanline - pixelStride; x += pixelStride) {
		float tmpVal = 0;

		for (int dy = -radius; dy <= radius; dy++) {
		    int tmpIndexY = y + dy;
		    if (0 <= tmpIndexY && tmpIndexY < height) {
			int offset = tmpIndexY * scanline;
			int kernelOffset = kernelWidth * (dy + radius) + radius;
			for (int dx = -radius; dx <= radius; dx++) {
			    float f = kernel[dx + kernelOffset];
			    if (f != 0) {
				int tmpIndexX = x + (dx * pixelStride);
				if (0 <= tmpIndexX && tmpIndexX < scanline) {
				    short pixel = pixelData[tmpIndexX + offset
					    + greenOffset];
				    tmpVal += f * (pixel & 0xffff);

				}
			    }

			}

		    }
		}
		tmpVal = tmpVal / 4f;
		//clamping
		tmpVal = tmpVal > 0xffff ? 0xffff : tmpVal < 0 ? 0 : tmpVal;

		output[x + y * scanline + greenOffset] = (short) tmpVal;
	    }

	}
	System.arraycopy(output, 0, pixelData, 0, output.length);

    }

    // detailed info: http://www.unc.edu/~rjean/demosaicing/demosaicing.pdf
    private void demosaicBilinear(WritableRaster raster) {
	LOGGER.info("start demosaicing");
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();
	for (int y = brink; y < raster.getHeight() - brink; y += 1) {
	    for (int x = brink * pixelStride; x < scanline - brink
		    * pixelStride; x += pixelStride) {
		int scanPos = x + y * scanline;

		int tmpR = db.getElem(scanPos);
		int tmpG = db.getElem(scanPos + greenOffset);
		int tmpB = db.getElem(scanPos + blueOffset);

		if (tmpR == 0 && tmpB == 0) {
		    //green pixel
		    int iR;
		    int iB;
		    if (db.getElem(scanPos + right) == 0) {
			//green in blue row, because neighboring red is 0
			iR = (db.getElem(scanPos + top) + db.getElem(scanPos
				+ bottom)) / 2;
			iB = (db.getElem(scanPos + left + blueOffset) + db
				.getElem(scanPos + right + blueOffset)) / 2;
		    } else {
			//green in red row
			iR = (db.getElem(scanPos + left) + db.getElem(scanPos
				+ right)) / 2;
			iB = (db.getElem(scanPos + top + blueOffset) + db
				.getElem(scanPos + bottom + blueOffset)) / 2;
		    }
		    iR = iR > 0xffff ? 0xffff : iR < 0 ? 0 : iR;
		    iB = iB > 0xffff ? 0xffff : iB < 0 ? 0 : iB;
		    //set interpolated r and b 
		    db.setElem(scanPos, iR);
		    db.setElem(scanPos + blueOffset, iB);

		} else if (tmpR == 0 && tmpG == 0) {
		    //blue pixel
		    //interpolate g and r
		    int iG = (db.getElem(scanPos + top + greenOffset)
			    + db.getElem(scanPos + bottom + greenOffset)
			    + db.getElem(scanPos + left + greenOffset) + db
			    .getElem(scanPos + right + greenOffset)) / 4;
		    int iR = (db.getElem(scanPos + topLeft)
			    + db.getElem(scanPos + topRight)
			    + db.getElem(scanPos + bottomLeft) + db
			    .getElem(scanPos + bottomRight)) / 4;
		    iR = iR > 0xffff ? 0xffff : iR < 0 ? 0 : iR;
		    iG = iG > 0xffff ? 0xffff : iG < 0 ? 0 : iG;
		    //set interpolated r and g 
		    db.setElem(scanPos, iR);
		    db.setElem(scanPos + greenOffset, iG);
		} else if (tmpG == 0 && tmpB == 0) {
		    //red pixel
		    //interpolate g and b
		    int iG = (db.getElem(scanPos + top + greenOffset)
			    + db.getElem(scanPos + bottom + greenOffset)
			    + db.getElem(scanPos + left + greenOffset) + db
			    .getElem(scanPos + right + greenOffset)) / 4;
		    int iB = (db.getElem(scanPos + topLeft + blueOffset)
			    + db.getElem(scanPos + topRight + blueOffset)
			    + db.getElem(scanPos + bottomLeft + blueOffset) + db
			    .getElem(scanPos + bottomRight + blueOffset)) / 4;
		    iG = iG > 0xffff ? 0xffff : iG < 0 ? 0 : iG;
		    iB = iB > 0xffff ? 0xffff : iB < 0 ? 0 : iB;
		    //set interpolated g and b
		    db.setElem(scanPos + greenOffset, iG);
		    db.setElem(scanPos + blueOffset, iB);

		}

	    }
	}

    }

    private void demosaicNearestNeighbor(WritableRaster raster) {
	LOGGER.info("start demosaicing");
	DataBufferUShort db = (DataBufferUShort) raster.getDataBuffer();

	for (int y = brink; y < raster.getHeight() - brink; y += 1) {
	    for (int x = brink * pixelStride; x < scanline - brink
		    * pixelStride; x += pixelStride) {
		int scanPos = x + y * scanline;

		int tmpR = db.getElem(scanPos);
		int tmpG = db.getElem(scanPos + greenOffset);
		int tmpB = db.getElem(scanPos + blueOffset);

		if (tmpR == 0 && tmpB == 0) {
		    //green pixel
		    int fillR;
		    int fillB;
		    if (db.getElem(scanPos + right) == 0) {
			//green in blue row neighboring red is 0
			fillR = db.getElem(scanPos + top);
			fillB = db.getElem(scanPos + right + blueOffset);
		    } else {
			//green in red row
			fillR = db.getElem(scanPos + left);
			fillB = db.getElem(scanPos + bottom + blueOffset);
		    }
		    //fill up 
		    db.setElem(scanPos, fillR);
		    db.setElem(scanPos + blueOffset, fillB);
		} else if (tmpG == 0 && tmpR == 0) {
		    //blue pixel
		    //fill up g and r
		    int fillG = db.getElem(scanPos + left + greenOffset);
		    int fillR = db.getElem(scanPos + topLeft);

		    //fill up r and g 
		    db.setElem(scanPos, fillR);
		    db.setElem(scanPos + greenOffset, fillG);
		} else if (tmpG == 0 && tmpB == 0) {
		    //red pixel
		    int fillG = db.getElem(scanPos + right + greenOffset);
		    int fillB = db.getElem(scanPos + bottomRight + blueOffset);

		    //fill up g and b
		    db.setElem(scanPos + greenOffset, fillG);
		    db.setElem(scanPos + blueOffset, fillB);

		}

	    }
	}

    }

}
