package com.jan_gruber.rawprocessor.model.engine.processor.operations;

import java.awt.image.WritableRaster;

import com.jan_gruber.rawprocessor.model.engine.store.RawImageContainer;
import com.jan_gruber.rawprocessor.model.engine.store.TIFFMetadata;

public abstract class PipelineComponent implements
		Comparable<PipelineComponent> {
	// specifies the position/ranking inside the pipeline
	protected int rank = -1;

	public PipelineComponent() {
	}

	public abstract void apply(RawImageContainer rawToProcess);

	@Override
	public int compareTo(PipelineComponent other) {
		if (this.rank > other.rank)
			return 1;
		else if (this.rank < other.rank)
			return -1;
		else
			return 0;
	}

}
