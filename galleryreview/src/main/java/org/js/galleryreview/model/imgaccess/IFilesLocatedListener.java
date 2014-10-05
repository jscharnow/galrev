package org.js.galleryreview.model.imgaccess;

import java.util.List;

public interface IFilesLocatedListener {
	public void newFilesParsed(List<PhysicalFile> newFiles);
}
