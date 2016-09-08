package com.jan_gruber.rawprocessor.model.engine.io;

import com.jan_gruber.rawprocessor.model.AbstractModel;
import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;

public abstract class RawImageReader extends AbstractModel {
	// property names
	public static final String IMAGE_LOADED = "imageLoaded";
	public static final String METADATA_LOADED = "metadataLoaded";

	public abstract void readFile(boolean readAdditionalThumbs);

}
