package com.jan_gruber.rawprocessor.model.engine.io;

import java.util.HashMap;

import com.jan_gruber.rawprocessor.model.engine.store.TIFFField;
import com.spinn3r.log5j.Logger;

public class IFD {
	private static final Logger LOGGER = Logger.getLogger();
	private int id;
	private long offsetInBytesFromZero;
	private int entryCount;
	private boolean hasSubDir;
	private boolean hasExifDir;
	private boolean hasMakernoteDir;

	private long offsetToSubDirectory;
	private IFD subDir;
	private HashMap<Integer, TIFFField> tagList = new HashMap<Integer, TIFFField>();

	protected long offsetToPixelData; // stripOffset tag
	protected long offsetToNextIFD;

	public IFD(long offset, int entryCount, int id) {
		this.offsetInBytesFromZero = offset;
		this.entryCount = entryCount;
		this.id = id;
	}

	public IFD(long offset, int entryCount) {
		this.offsetInBytesFromZero = offset;
		this.entryCount = entryCount;
	}

	protected void addTag(TIFFField tagToAdd) {
		tagList.put(Integer.valueOf(tagToAdd.getCode()), tagToAdd);
	}

	public TIFFField getTag(int key) {
		if (tagList.containsKey(key))
			return tagList.get(key);
		else
			return null;

	}

	public long getOffsetInBytesFromZero() {
		return offsetInBytesFromZero;
	}

	public void setOffsetInBytesFromZero(long offsetInBytesFromZero) {
		this.offsetInBytesFromZero = offsetInBytesFromZero;
	}

	public long getOffsetToNextIFD() {
		return offsetToNextIFD;
	}

	public void setOffsetToNextIFD(long offsetToNextIFD) {
		this.offsetToNextIFD = offsetToNextIFD;
	}

	public int getEntryCount() {
		return entryCount;
	}

	public void setEntryCount(int entryCount) {
		this.entryCount = entryCount;
	}

	public HashMap<Integer, TIFFField> getTagList() {
		return tagList;
	}

	public void setTagList(HashMap<Integer, TIFFField> tagList) {
		this.tagList = tagList;
	}

	public boolean hasSubDirectory() {
		return hasSubDir;
	}

	public void setHasSubDirectory(boolean hasSubDirectories) {
		this.hasSubDir = hasSubDirectories;
	}

	public boolean hasExifDir() {
		return hasExifDir;
	}

	public void setHasExifDir(boolean hasExifDir) {
		this.hasExifDir = hasExifDir;
	}

	public boolean hasMakernoteDir() {
		return hasMakernoteDir;
	}

	public void setHasMakernoteDir(boolean hasMakernoteDir) {
		this.hasMakernoteDir = hasMakernoteDir;
	}

	public long getOffsetToSubDirectory() {
		return offsetToSubDirectory;
	}

	public void setOffsetToSubDirectory(long offsetToSubDirectory) {
		this.offsetToSubDirectory = offsetToSubDirectory;
	}

	public IFD getSubDir() {
		return subDir;
	}

	public void setSubDir(IFD subDir) {
		this.subDir = subDir;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
