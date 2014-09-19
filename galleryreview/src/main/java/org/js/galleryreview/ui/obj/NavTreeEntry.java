package org.js.galleryreview.ui.obj;

import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.ui.i18n.Texts;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NavTreeEntry {
	private NavEntryType type;
	
	private String directoryPath;
	private String fileName;

	private StringProperty identification = new SimpleStringProperty();

	private Location location;
	
	public NavTreeEntry(NavEntryType type) {
		this.type = type;
		updateIdentification();
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
		updateIdentification();
	}
	
	public Location getLocation() {
		return location;
	}
	
	private void updateIdentification() {
		if (null != type){
			switch (type){
			case DIRECTORY:
				identification.set(getDirectoryPath());
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
