package com.jan_gruber.rawprocessor.model.engine.io;

import java.util.HashMap;

public class TagDictionary {
    private static HashMap<Integer, String> baselineAndExtendedTags = new HashMap<Integer, String>();
    static {
	baselineAndExtendedTags.put(0xFE, "NewSubFileType");
	baselineAndExtendedTags.put(0xFF, "SubFileType");
	baselineAndExtendedTags.put(0x100, "ImageWidth");
	baselineAndExtendedTags.put(0x101, "ImageHeight");
	baselineAndExtendedTags.put(0x102, "ImageLength");
	baselineAndExtendedTags.put(0x103, "ImageLength");
	baselineAndExtendedTags.put(0x102, "BitsPerSample");
	baselineAndExtendedTags.put(0x103, "Compression");
	baselineAndExtendedTags.put(0x106, "PhotometricInterpretation");
	baselineAndExtendedTags.put(0x107, "Threshholding");
	baselineAndExtendedTags.put(0x108, "CellWidth");
	baselineAndExtendedTags.put(0x109, "CellLength");
	baselineAndExtendedTags.put(0x10A, "FillOrder");
	baselineAndExtendedTags.put(0x10D, "DocumentName");
	baselineAndExtendedTags.put(0x10E, "ImageDescription");
	baselineAndExtendedTags.put(0x10F, "Make");
	baselineAndExtendedTags.put(0x110, "Model");
	baselineAndExtendedTags.put(0x111, "StripOffsets");
	baselineAndExtendedTags.put(0x112, "Orientation");
	baselineAndExtendedTags.put(0x115, "SamplesPerPixel");
	baselineAndExtendedTags.put(0x116, "RowsPerStrip");
	baselineAndExtendedTags.put(0x117, "StripByteCounts");
	baselineAndExtendedTags.put(0x118, "MinSampleValue");
	baselineAndExtendedTags.put(0x119, "MaxSampleValue");
	baselineAndExtendedTags.put(0x11A, "XResolution");
	baselineAndExtendedTags.put(0x11B, "YResolution");
	baselineAndExtendedTags.put(0x11C, "PlanarConfiguration");
	baselineAndExtendedTags.put(0x11D, "PageName");
	baselineAndExtendedTags.put(0x11E, "XPosition");
	baselineAndExtendedTags.put(0x11F, "YPosition");
	baselineAndExtendedTags.put(0x120, "FreeOffsets");
	baselineAndExtendedTags.put(0x121, "FreeByteCounts");
	baselineAndExtendedTags.put(0x122, "GrayResponseUnit");
	baselineAndExtendedTags.put(0x123, "GrayResponseCurve");
	baselineAndExtendedTags.put(0x124, "T4Options");
	baselineAndExtendedTags.put(0x125, "T6Options");
	baselineAndExtendedTags.put(0x128, "ResolutionUnit");
	baselineAndExtendedTags.put(0x129, "PageNumber");
	baselineAndExtendedTags.put(0x12D, "TransferFunction");
	baselineAndExtendedTags.put(0x131, "Software");
	baselineAndExtendedTags.put(0x132, "DateTime");
	baselineAndExtendedTags.put(0x13B, "Artist");
	baselineAndExtendedTags.put(0x13C, "HostComputer");
	baselineAndExtendedTags.put(0x13D, "Predictor");
	baselineAndExtendedTags.put(0x13E, "WhitePoint");
	baselineAndExtendedTags.put(0x13F, "PrimaryChromaticities");
	baselineAndExtendedTags.put(0x140, "ColorMap");
	baselineAndExtendedTags.put(0x141, "HalftoneHints");
	baselineAndExtendedTags.put(0x142, "TileWidth");
	baselineAndExtendedTags.put(0x143, "TileLength");
	baselineAndExtendedTags.put(0x144, "TileOffsets");
	baselineAndExtendedTags.put(0x145, "TileByteCounts");
	baselineAndExtendedTags.put(0x146, "BadFaxLines");
	baselineAndExtendedTags.put(0x147, "CleanFaxData");
	baselineAndExtendedTags.put(0x148, "ConsecutiveBadFaxLines");
	baselineAndExtendedTags.put(0x14A, "InkSet");
	baselineAndExtendedTags.put(0x14D, "InkNames");
	baselineAndExtendedTags.put(0x14E, "NumberOfInks");
	baselineAndExtendedTags.put(0x150, "DotRange");
	baselineAndExtendedTags.put(0x151, "TargetPrinter");
	baselineAndExtendedTags.put(0x152, "ExtraSamples");
	baselineAndExtendedTags.put(0x153, "SampleFormat");
	baselineAndExtendedTags.put(0x154, "SMinSampleValue");
	baselineAndExtendedTags.put(0x155, "SMaxSampleValue");
	// #########

    }

    private static HashMap<Integer, String> exifTags = new HashMap<Integer, String>();
    static {
	exifTags.put(0x829A, "ExposureTime");
	exifTags.put(0x829D, "FNumber");
	exifTags.put(0x8822, "ExposureProgram");
	exifTags.put(0x8824, "SpectralSensitivity");
	exifTags.put(0x8827, "ISOSpeedRatings");
	exifTags.put(0x8828, "OECF");
	exifTags.put(0x9000, "ExifVersion");
	exifTags.put(0x9003, "DateTimeOriginal");
	exifTags.put(0x9004, "DateTimeDigitized");
	exifTags.put(0x9101, "ComponentsConfiguration");
	exifTags.put(0x9102, "CompressedBitsPerPixel");
	exifTags.put(0x9201, "ShutterSpeedValue");
	exifTags.put(0x9202, "ApertureValue");
	exifTags.put(0x9203, "BrightnessValue");
	exifTags.put(0x9204, "ExposureBiasValue");
	exifTags.put(0x9205, "MaxApertureValue");
	exifTags.put(0x9206, "SubjectDistance");
	exifTags.put(0x9207, "MeteringMode");
	exifTags.put(0x9208, "LightSource");
	exifTags.put(0x9209, "Flash");
	exifTags.put(0x920A, "FocalLength");
	exifTags.put(0x920B, "FlashEnergy");
	exifTags.put(0x9214, "SubjectLocation");
	//##############################
	exifTags.put(0x927C, "MakerNote");
	exifTags.put(0x9286, "UserComment");
	exifTags.put(0x9290, "SubsecTime");
	exifTags.put(0x9291, "SubsecTimeOriginal");
	exifTags.put(9292, "SubsecTimeDigitized");
	exifTags.put(0x935C, "ImageSourceData");
	exifTags.put(0xA000, "FlashpixVersion");
	exifTags.put(0xA001, "ColorSpace");
	exifTags.put(0xA002, "PixelXDimension");
	exifTags.put(0xA003, "PixelYDimension");
	exifTags.put(0xA004, "RelatedSoundFile");
	exifTags.put(0xA005, "Interoperability IFD");
	exifTags.put(0xA20B, "FlashEnergy");
	exifTags.put(0xA20C, "SpatialFrequencyResponse");
	exifTags.put(0xA20E, "FocalPlaneXResolution");
	exifTags.put(0xA20F, "FocalPlaneYResolution");
	exifTags.put(0xA210, "FocalPlaneResolutionUnit");
	exifTags.put(0xA214, "SubjectLocation");
	exifTags.put(0xA215, "ExposureIndex");
	exifTags.put(0xA217, "SensingMethod");
	exifTags.put(0xA300, "FileSource");
	exifTags.put(0xA301, "SceneType");
	exifTags.put(0xA302, "CFAPattern");
	exifTags.put(0xA401, "CustomRendered");
	exifTags.put(0xA402, "ExposureMode");
	exifTags.put(0xA403, "WhiteBalance");
	exifTags.put(0xA404, "DigitalZoomRatio");
	exifTags.put(0xA405, "FocalLengthIn35mmFilm");
	exifTags.put(0xA406, "SceneCaptureType");
	exifTags.put(0xA407, "GainControl");
	exifTags.put(0xA408, "Contrast");
	exifTags.put(0xA409, "Saturation");
	exifTags.put(0xA40A, "Sharpness");
	exifTags.put(0xA40B, "DeviceSettingDescription");
	exifTags.put(0xA40C, "SubjectDistanceRange");
	exifTags.put(0xA420, "ImageUniqueID");
	//##################################

    }

    private static HashMap<Integer, String> canonMakernoteTags = new HashMap<Integer, String>();
    static {
	canonMakernoteTags.put(0x0001, "ExposureInfo");
	canonMakernoteTags.put(0x0002, "Focal Length");
	canonMakernoteTags.put(0x0003, "FlashInfo");
	canonMakernoteTags.put(0x0005, "Panorama");
	canonMakernoteTags.put(0x0006, "ImageType");
	canonMakernoteTags.put(0x0007, "Firmware Version");
	canonMakernoteTags.put(0x0008, "ImageNumber");
	canonMakernoteTags.put(0x0009, "OwnerName");
	canonMakernoteTags.put(0x000c, "CameraSerialNumber");
	canonMakernoteTags.put(0x000d, "CameraInfo");
	canonMakernoteTags.put(0x4001, "RGGB_Level_As_Shot");
	canonMakernoteTags.put(0x4003, "ColorInfo");
	canonMakernoteTags.put(0x4008, "BlackLevel");
	//############################################

    }

    public static String translate(int key) {
	Integer iKey = Integer.valueOf(key);
	if (baselineAndExtendedTags.containsKey(iKey))
	    return baselineAndExtendedTags.get(iKey);
	else if (exifTags.containsKey(iKey))
	    return exifTags.get(iKey);
	else
	    return Integer.toString(iKey);
    }

}
