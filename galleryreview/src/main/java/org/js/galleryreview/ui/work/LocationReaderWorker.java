package org.js.galleryreview.ui.work;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.imgaccess.IFilesParsedListener;
import org.js.galleryreview.model.imgaccess.ImageLocator;
import org.js.galleryreview.model.imgaccess.PhysicalFile;
import org.js.galleryreview.model.provider.ReviewProvider;
import org.js.galleryreview.ui.obj.NavEntryType;
import org.js.galleryreview.ui.obj.NavTreeEntry;

public class LocationReaderWorker implements Runnable, IFilesParsedListener{

	private static final int DELTA_LOCATION = 50;
	private NavTreeEntry baseLocation;
	private HashMap<String, ImageFile> storedFiles = new HashMap<>();
	private TreeItem<NavTreeEntry> baseTreeItem;

	public LocationReaderWorker(TreeItem<NavTreeEntry> baseTreeItem){
		this.baseTreeItem = baseTreeItem;
		this.baseLocation = baseTreeItem.getValue();
		for (ImageFile file: baseLocation.getLocation().getFiles()){
			storedFiles.put(file.getPath(), file);
		}
	}
	
	@Override
	public void run() {
		ImageLocator locator = new ImageLocator(baseLocation.getLocation().getPath());
		locator.addFilesParsedListener(DELTA_LOCATION, this);
		locator.readFiles();
	}

	@Override
	public void newFilesParsed(List<PhysicalFile> newFiles) {
		for (PhysicalFile newPf: newFiles){
			String path = newPf.getFile().getAbsolutePath();
			if (!storedFiles.containsKey(path)){
				ImageFile file = new ImageFile();
				file.setFkLocation(baseLocation.getLocation().getId());
				file.setPath(path);
				file.setName(newPf.getFilename());
				ReviewProvider.getInstance().mergeFile(file); // TODO: Exception handling
				storedFiles.put(path, file);
			}
			File file = newPf.getFile();
			ObservableList<TreeItem<NavTreeEntry>> children = baseTreeItem.getChildren();
			NavTreeEntry nte = new NavTreeEntry(file);
			TreeItem<NavTreeEntry> ti = new TreeItem<NavTreeEntry>(nte);
			if (file.getParent().equals(baseLocation.getDirectoryPath())){
				children.add(ti);
			}else{
				// locate parent
				String parentPath = newPf.getFile().getParent();
				TreeItem<NavTreeEntry> parentTi = getParentTi(children,
						parentPath);
				parentTi.getChildren().add(ti);
			}
		}

	}

	private TreeItem<NavTreeEntry> getParentTi(
			ObservableList<TreeItem<NavTreeEntry>> children, String parentPath) {
		TreeItem<NavTreeEntry> parentTi = null;
		for (TreeItem<NavTreeEntry> ti: children){
			NavTreeEntry nte = ti.getValue();
			if (nte.getType() == NavEntryType.DIRECTORY){
				String nteDirectory = nte.getDirectoryPath();
				if (nteDirectory.equals(parentPath)){
					parentTi = ti;
				}else if (parentPath.startsWith(nteDirectory)){
					parentTi = getParentTi(ti.getChildren(), parentPath);
				}
			}
		}
		return parentTi;
	}

}
