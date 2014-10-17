package org.js.galleryreview.ui.work;

import static org.js.galleryreview.ui.UiHelper.addImageFileToTreeItem;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.imgaccess.IFilesLocatedListener;
import org.js.galleryreview.model.imgaccess.ImageLocator;
import org.js.galleryreview.model.imgaccess.PhysicalFile;
import org.js.galleryreview.model.provider.ReviewProvider;
import org.js.galleryreview.ui.obj.NavTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LocationReaderWorker is the interface between the file location and
 * the UI rendering the parse results. The class implements
 * {@link IFilesLocatedListener} to be provided to an {@link ImageLocator}.
 * Whenever (a subset of) files are located, they are added to the
 * {@link TreeTableView} below the {@link TreeItem} representing the root
 * directory of the files located by the associated locator.
 */
public class LocationReaderWorker implements Runnable, IFilesLocatedListener {

	private static final int DELTA_LOCATION = 5;
	private HashMap<String, ImageFile> storedFiles = new HashMap<>();
	private TreeItem<NavTreeEntry> baseTreeItem;
	private TreeItem<NavTreeEntry> toDeleteBaseTreeItem;
	private boolean finished;
	private static Logger logger = LoggerFactory.getLogger(LocationReaderWorker.class);

	public LocationReaderWorker(TreeItem<NavTreeEntry> baseTreeItem, TreeItem<NavTreeEntry> toDeleteBaseTreeItem) {
		this.baseTreeItem = baseTreeItem;
		this.toDeleteBaseTreeItem = toDeleteBaseTreeItem;
		NavTreeEntry baseLocation = baseTreeItem.getValue();
		for (ImageFile file : baseLocation.getLocation().getFiles()) {
			storedFiles.put(file.getPath(), file);
		}
	}

	@Override
	public void run() {
		ImageLocator locator = new ImageLocator(baseTreeItem.getValue().getLocation()
				.getPath());
		locator.addFilesParsedListener(DELTA_LOCATION, this);
		locator.readFiles();
		finished = true;
	}

	@Override
	public void newFilesParsed(List<PhysicalFile> newFiles) {
		logger.debug("New files parsed: " + newFiles.size());
		// run in UI thread
		Platform.runLater(() -> {
			// try to add each file. The list of new files is contains also
			// directories. It is assumed,
			// that containing directories are added before child files
			for (PhysicalFile newPf : newFiles) {
				File file = newPf.getFile();
				String path = file.getAbsolutePath();
				// new file? If yes, the file has to be added to the database as
				// new
				if (!storedFiles.containsKey(path)) {
					ImageFile imgFile = new ImageFile();
					imgFile.setFkLocation(baseTreeItem.getValue().getLocation().getId());
					imgFile.setPath(path);
					imgFile.setName(newPf.getFilename());
					imgFile = ReviewProvider.getInstance().mergeFile(imgFile);
					// TODO: Exception handling
					storedFiles.put(path, imgFile);
				}
				ImageFile imageFile = storedFiles.get(path);
				if (imageFile.isFlaggedToDelete()){
					NavTreeEntry nte = new NavTreeEntry(file, imageFile);
					TreeItem<NavTreeEntry> ti = new TreeItem<NavTreeEntry>(nte);
					toDeleteBaseTreeItem.getChildren().add(ti);
				} else {
					addImageFileToTreeItem(imageFile, baseTreeItem);
				}
			}
		});
	}

	
	
	public boolean isFinished() {
		return finished;
	}

}
