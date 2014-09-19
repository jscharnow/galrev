package org.js.galleryreview.model.imgaccess;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageLocator {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private int filesRead;
	private String rootPath;
	
	public ImageLocator(String rootPath){
		this.rootPath = rootPath;
	}

	public List<PhysicalFile> readFiles(){
		File root = new File(rootPath);
		logger.debug("Root: " + root);
		List<PhysicalFile> fileList = Collections.emptyList();
		if (root.exists() && root.isDirectory()){
			PhysicalFile rootPf = new PhysicalFile(root);
			readFilesFromDir(rootPf);
			fileList = rootPf.getChildren();
		}else{
			logger.debug("Root is no existing directory");
		}
		return fileList;
	}

	private void readFilesFromDir(PhysicalFile root) {
		logger.trace("Read from dir " + root);
		for (File aFile: root.getFile().listFiles()){
			root.addChild(aFile);
			fileAdded();
		}
		for (PhysicalFile aFile: root.getChildren()){
			if (aFile.isDirectory()){
				readFilesFromDir(aFile);
			}
		}
	}

	private void fileAdded() {
		filesRead++;
	} 
	
	public int getFilesRead() {
		return filesRead;
	}
	
	
}
