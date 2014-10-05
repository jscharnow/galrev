package org.js.galleryreview.ui.work;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.imgaccess.IFilesLocatedListener;
import org.js.galleryreview.model.imgaccess.ImageLocator;
import org.js.galleryreview.model.imgaccess.PhysicalFile;
import org.js.galleryreview.model.provider.ReviewProvider;
import org.js.galleryreview.ui.obj.NavEntryType;
import org.js.galleryreview.ui.obj.NavTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LocationReaderWorker is the interface between the file location and
 * the UI renderering the parse results. The class implements
 * {@link IFilesLocatedListener} to be provided to an {@link ImageLocator}.
 * Whenever (a subset of) files are located, they are added to the
 * {@link TreeTableView} below the {@link TreeItem} representing the root
 * directory of the files located by the associated locator.
 */
public class LocationReaderWorker implements Runnable, IFilesLocatedListener {

	private static final int DELTA_LOCATION = 5;
	private NavTreeEntry baseLocation;
	private HashMap<String, ImageFile> storedFiles = new HashMap<>();
	private TreeItem<NavTreeEntry> baseTreeItem;
	private boolean finished;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public LocationReaderWorker(TreeItem<NavTreeEntry> baseTreeItem) {
		this.baseTreeItem = baseTreeItem;
		this.baseLocation = baseTreeItem.getValue();
		for (ImageFile file : baseLocation.getLocation().getFiles()) {
			storedFiles.put(file.getPath(), file);
		}
	}

	@Override
	public void run() {
		ImageLocator locator = new ImageLocator(baseLocation.getLocation()
				.getPath());
		locator.addFilesParsedListener(DELTA_LOCATION, this);
		locator.readFiles();
		finished = true;
	}

	@Override
	public void newFilesParsed(List<PhysicalFile> newFiles) {
		// run in UI thread
		Platform.runLater(() -> {
			// try to add each file. The list of new files is contains also
			// directories. It is assumed,
			// that containing directories are added before child files
			for (PhysicalFile newPf : newFiles) {
				String path = newPf.getFile().getAbsolutePath();
				// new file? If yes, the file has to be added to the database as
				// new
				if (!storedFiles.containsKey(path)) {
					ImageFile file = new ImageFile();
					file.setFkLocation(baseLocation.getLocation().getId());
					file.setPath(path);
					file.setName(newPf.getFilename());
					ReviewProvider.getInstance().mergeFile(file);
					// TODO: Exception handling
					storedFiles.put(path, file);
				}
				File file = newPf.getFile();
				ObservableList<TreeItem<NavTreeEntry>> children = baseTreeItem
						.getChildren();
				NavTreeEntry nte = new NavTreeEntry(file);
				TreeItem<NavTreeEntry> ti = new TreeItem<NavTreeEntry>(nte);
				String fileParentPath = file.getParent();
				logger.debug("Compare root path " + baseLocation.getDirectoryPath() +" with file directory " + fileParentPath) ;
				if (fileParentPath.equals(baseLocation.getDirectoryPath())) {
					// item is located directly below root
					addItemToChildList(children, ti);
				} else {
					// item in subdirectory -> locate parent
					String parentPath = newPf.getFile().getParent();
					TreeItem<NavTreeEntry> parentTi = getParentTi(children,
							parentPath);
					if (null != parentTi) {
						addItemToChildList(parentTi.getChildren(),ti);
					} else {
						LoggerFactory.getLogger(getClass())
								.error("Parent null");
					}
				}
			}
		});
	}

	/**
	 * Adds the item to child list and sorts list.
	 *
	 * @param childList the child list
	 * @param newItem the new item
	 */
	private void addItemToChildList(
			ObservableList<TreeItem<NavTreeEntry>> childList,
			TreeItem<NavTreeEntry> newItem) {
		childList.add(newItem);
		Collections.sort(childList, getFileComparator());
	}

	private Comparator<TreeItem<NavTreeEntry>> getFileComparator() {
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

	public boolean isFinished() {
		return finished;
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
	private TreeItem<NavTreeEntry> getParentTi(
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
