package org.js.galleryreview.ui.obj;

import java.io.File;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.ui.i18n.Texts;
import org.slf4j.LoggerFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NavTreeEntry {
	private NavEntryType type;
	
	private String directoryPath;
	private String fileName;

	private StringProperty identification = new SimpleStringProperty();

	private Location location;

	private ImageFile imageFile;
	
	public NavTreeEntry(NavEntryType type) {
		this();
		this.type = type;
		updateIdentification();
	}
	
	public NavTreeEntry(File file, ImageFile imageFile) {
		this();
		this.imageFile = imageFile;
		if (file.isDirectory()){
			type = NavEntryType.DIRECTORY;
			directoryPath = file.getAbsolutePath();
			fileName = file.getName();
		}else{
			type = NavEntryType.FILE;
			fileName = file.getName();
			directoryPath = file.getParent();
		}
		updateIdentification();
	}

	private NavTreeEntry() {
		LoggerFactory.getLogger(getClass()).trace("NavTreeEntry created");
	}

	public String getDirectoryPath() {
		return directoryPath;
	}
	public String getFileName() {
		return fileName;
	}
	public NavEntryType getType() {
		return type;
	}
	public StringProperty identificationProperty(){
		return identification ;
	}
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
		updateIdentification();
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
		updateIdentification();
	}
	public void setType(NavEntryType type) {
		this.type = type;
		updateIdentification();
	}

	public void setLocation(Location location) {
		this.location = location;
		if (null != location){
			type=NavEntryType.LOCATION;
			directoryPath = location.getPath();
		}
		updateIdentification();
	}
	
	public Location getLocation() {
		return location;
	}
	
	public ImageFile getImageFile() {
		return imageFile;
	}
	
	public void setImageFile(ImageFile imageFile) {
		this.imageFile = imageFile;
	}
	
	@Override
	public String toString() {
		return "NavTreeEntry [type=" + type + ", identification="
				+ identification + "]";
	}

	private void updateIdentification() {
		if (null != type){
			switch (type){
			case DIRECTORY:
				String dir = getDirectoryPath();
				// only name of directory
				identification.set(new File(dir).getName()); 
				break;
			case FILE:
				identification.set(getFileName());
				break;
			case LOCATIONS:
				identification.set(Texts.getText("tree.repository"));
				break;
			case TO_DELETE:
				identification.set(Texts.getText("tree.toDelete"));
				break;
			case ROOT_REVIEW:
				identification.set(Texts.getText("tree.review"));
				break;
			case LOCATION:
				if (null != location){
					identification.set(getLocation().getName());
				}
				break;
			default:
				break;
			}
		}else{
			identification.set("");
		}
	}

}
