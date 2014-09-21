package org.js.galleryreview.model.imgaccess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageLocator {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private int filesRead;
	private int lastNotified = -1;
	private String rootPath;
	private HashMap<Integer, List<IFilesParsedListener>> updateListeners = new HashMap<Integer, List<IFilesParsedListener>>();
	private List<PhysicalFile> allFilesRead = new ArrayList<PhysicalFile>();

	
	public ImageLocator(String rootPath){
		this.rootPath = rootPath;
	}

	public List<PhysicalFile> readFiles(){
		File root = new File(rootPath);
		logger.debug("Read files from root: " + root);
		List<PhysicalFile> fileList = Collections.emptyList();
		if (root.exists() && root.isDirectory()){
			PhysicalFile rootPf = new PhysicalFile(root);
			readFilesFromDir(rootPf);
			fileList = rootPf.getChildren();
		}else{
			logger.debug("Root is no existing directory");
		}
		for (Integer aVal: updateListeners.keySet()){
			notify(aVal);
		}
		return fileList;
	}

	private void readFilesFromDir(PhysicalFile root) {
		logger.trace("Read from dir " + root);
		for (File aFile: root.getFile().listFiles()){
			fileAdded(root.addChild(aFile));
		}
		for (PhysicalFile aFile: root.getChildren()){
			if (aFile.isDirectory()){
				readFilesFromDir(aFile);
			}
		}
	}

	private void fileAdded(PhysicalFile pf) {
		allFilesRead.add(pf);
		filesRead++;
		logger.trace("File added -> " + pf+" ("+ filesRead+")");
		boolean notified = false;
		for (Integer aVal: updateListeners.keySet()){
			if (filesRead % aVal.intValue() == 0){
				logger.trace("Notify");
				notify(aVal);
				notified = true;
			}
		}
		if (notified){
			lastNotified = filesRead;
		}
	}

	private void notify(Integer aVal) {
		if (lastNotified != filesRead){
			int notificationCount = filesRead / aVal.intValue();
			if (filesRead % aVal.intValue()!= 0){
				// this is the case if the remaining files shall be notified
				notificationCount ++;
			}
			int startIdx = (notificationCount-1) * aVal.intValue();
			int endIdx = startIdx + aVal.intValue();
			List<PhysicalFile> subList;
			logger.trace(""+startIdx +" -> " + endIdx);
			if (endIdx > filesRead){
				subList = allFilesRead.subList(startIdx, filesRead);
			}else{
				subList = allFilesRead.subList(startIdx, endIdx);
			}

			//TODO this might not be necessary
			ArrayList<PhysicalFile> notificationList = new ArrayList<>(subList);
			for (IFilesParsedListener l: updateListeners.get(aVal)){
				l.newFilesParsed(notificationList);
			}
		}
	} 
	
	public int getFilesRead() {
		return filesRead;
	}

	/**
	 * Sets the files parsed listener.
	 *
	 * @param deltaNotifation the numbers of files parsed when the listener is invoked
	 * @param listener the listener
	 */
	public void addFilesParsedListener(int deltaNotifation, IFilesParsedListener listener) {
		Integer key = new Integer(deltaNotifation);
		if (!updateListeners.containsKey(key)){
			updateListeners.put(key, new ArrayList<IFilesParsedListener>());
		}
		updateListeners.get(key).add(listener);
	}
	
	
}
