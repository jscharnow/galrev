package org.js.galleryreview.model.imgaccess;

import java.time.LocalDateTime;

public class ImageMetaData {

	private LocalDateTime dateTimeOriginal;
	private long sizeBytes;
	private int height;
	private int width;

	public LocalDateTime getDateTimeOriginal() {
		return dateTimeOriginal;
	}

	public void setDateTimeOriginal(LocalDateTime dateTimeOriginal) {
		this.dateTimeOriginal = dateTimeOriginal;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public void setSizeBytes(long sizeBytes) {
		this.sizeBytes = sizeBytes;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return "ImageMetadata [dateTimeOriginal=" + dateTimeOriginal
				+ ", sizeBytes=" + sizeBytes + ", height=" + height
				+ ", width=" + width + "]";
	}
}
