package org.js.galleryreview.model.entities;

import javax.persistence.Entity;
import java.io.Serializable;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;
import java.lang.Override;

@Entity
@Table(name = "image_file")
public class ImageFile implements Serializable {

	/**
    * 
    */
	private static final long serialVersionUID = 6648300529902446195L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 300)
	private String path;

	@Column(name = "reviewed")
	private boolean reviewed;

	@Column(name = "to_delete")
	private boolean flaggedToDelete;

	@Column(name = "deleted")
	private boolean deleted;

	@Column(name = "fk_location")
	private long fkLocation;

	@Column(name = "name")
	private String name;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ImageFile)) {
			return false;
		}
		ImageFile other = (ImageFile) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public boolean isFlaggedToDelete() {
		return flaggedToDelete;
	}

	public void setFlaggedToDelete(boolean flaggedToDelete) {
		this.flaggedToDelete = flaggedToDelete;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public long getFkLocation() {
		return fkLocation;
	}

	public void setFkLocation(long idLocation) {
		this.fkLocation = idLocation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (path != null && !path.trim().isEmpty())
			result += ", path: " + path;
		result += ", reviewed: " + reviewed;
		result += ", flaggedToDelete: " + flaggedToDelete;
		result += ", deleted: " + deleted;
		result += ", fkLocation: " + fkLocation;
		if (name != null && !name.trim().isEmpty())
			result += ", name: " + name;
		return result;
	}
}