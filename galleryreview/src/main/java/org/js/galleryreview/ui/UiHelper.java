package org.js.galleryreview.ui;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.ui.obj.NavEntryType;
import org.js.galleryreview.ui.obj.NavTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class UiHelper provides miscellaneous helper methods.
 */
public class UiHelper {

	private static Logger logger  = LoggerFactory.getLogger(UiHelper.class);
	
	/**
	 * Adds the image file to tree item by traversing to the parent directory item if necessary.
	 *
	 * @param imageFile the image file
	 * @param baseItem the base item
	 */
	public static void addImageFileToTreeItem(ImageFile imageFile,
			TreeItem<NavTreeEntry> baseItem) {
		switch (baseItem.getValue().getType()){
		case LOCATION:
		case DIRECTORY:
		case FILE:
			addFileToLocation(imageFile, baseItem);
			break;
		case LOCATIONS:
			for (TreeItem<NavTreeEntry> aChild: baseItem.getChildren()){
				boolean added=addFileToLocation(imageFile, aChild);
				if (added){
					break;
				}
			}
			break;
		default:
			logger.error("Request to add image file " + imageFile +" to tree item of unsupported type " + baseItem.getValue());
			break;
		}
		
	}

	private static boolean addFileToLocation(ImageFile imageFile,
			TreeItem<NavTreeEntry> baseItem) {
		boolean added=false;
		ObservableList<TreeItem<NavTreeEntry>> children = baseItem
				.getChildren();
		File aFile=new File(imageFile.getPath());
		NavTreeEntry nte = new NavTreeEntry(aFile, imageFile);
		TreeItem<NavTreeEntry> ti = new TreeItem<NavTreeEntry>(nte);
		String fileParentPath = aFile.getParent();
		logger.debug("Compare root path "
				+ baseItem.getValue().getDirectoryPath()
				+ " with file directory " + fileParentPath);
		if (fileParentPath.equals(baseItem.getValue().getDirectoryPath())) {
			// item is located directly below root
			added=addItemToChildList(children, ti);
		} else {
			// item in subdirectory -> locate parent
			String parentPath = aFile.getParent();
			TreeItem<NavTreeEntry> parentTi = getParentTi(children,
					parentPath);
			if (null != parentTi) {
				added=addItemToChildList(parentTi.getChildren(), ti);
			} else {
				logger.error("Parent of file " + imageFile + " not found");
				added = false;
			}
		}
		return added;
	}

	/**
	 * Adds the item to child list and sorts list.
	 *
	 * @param childList the child list
	 * @param newItem the new item
	 * @return 
	 */
	private static boolean addItemToChildList(
			ObservableList<TreeItem<NavTreeEntry>> childList,
			TreeItem<NavTreeEntry> newItem) {
		childList.add(newItem);
		Collections.sort(childList, getFileComparator());
		return true;
	}

	private static Comparator<TreeItem<NavTreeEntry>> getFileComparator() {
		return new Comparator<TreeItem<NavTreeEntry>>() {
			
			@Override
			public int compare(TreeItem<NavTreeEntry> o1, TreeItem<NavTreeEntry> o2) {
				int cmpResult;
				NavTreeEntry nte1 = o1.getValue();
				NavTreeEntry nte2 = o2.getValue();
				if (nte1.getType() == NavEntryType.DIRECTORY && nte2.getType() == NavEntryType.FILE){
					cmpResult = -1;
				}else if (nte2.getType() == NavEntryType.DIRECTORY && nte1.getType() == NavEntryType.FILE){
					cmpResult = 1;
				}else{
					cmpResult = nte1.identificationProperty().getValue().compareToIgnoreCase(nte2.identificationProperty().getValue());
				}
				return cmpResult;
			}
		};
	}
	

	/**
	 * Gets the parent treeitem, i.e. the tree item displaying the given path.
	 *
	 * @param itemCandidates
	 *            the item candidates
	 * @param parentPath
	 *            the parent path
	 * @return the parent treeitem
	 */
	private static TreeItem<NavTreeEntry> getParentTi(
			ObservableList<TreeItem<NavTreeEntry>> itemCandidates,
			String parentPath) {
		logger.debug("Search tree item for pareht path " + parentPath
				+ ", #candidates: " + itemCandidates.size());
		TreeItem<NavTreeEntry> parentTi = null;
		for (TreeItem<NavTreeEntry> ti : itemCandidates) {
			NavTreeEntry nte = ti.getValue();
			logger.debug("Got candidate " + nte);
			if (nte.getType() == NavEntryType.DIRECTORY) {
				String nteDirectory = nte.getDirectoryPath();
				logger.debug("Found parent directory to compare with: "
						+ nteDirectory);
				if (nteDirectory.equals(parentPath)) {
					parentTi = ti;
				} else if (parentPath.startsWith(nteDirectory)) {
					parentTi = getParentTi(ti.getChildren(), parentPath);
				}
			}
		}
		return parentTi;
	}


}
