package org.js.galleryreview.model.imgaccess;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class PhysicalFile implements Serializable{

	private static final long serialVersionUID = 6097593288366835343L;
	
	private File file;
	private List<PhysicalFile> children = new ArrayList<PhysicalFile>();

	public PhysicalFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}

	public void addChild(File child){
		PhysicalFile childPf = new PhysicalFile(child);
		children.add(childPf);
	}
	
	public List<PhysicalFile> getChildren() {
		return children;
	}
	
	public boolean isDirectory(){
		return file.isDirectory();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhysicalFile other = (PhysicalFile) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PhysicalFile [file=" + file + "]";
	}

	public String getFilename() {
		return file.getName();
	}

}
