package org.js.galleryreview.model.entities;

import javax.persistence.Entity;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "location")
public class Location implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7266820414903239811L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 200, name = "name")
	private String name;

	@Column(length = 300, name = "path")
	private String path;
	
	@OneToMany
	@JoinColumn(name = "fk_location")
	private List<ImageFile> files = new ArrayList<ImageFile>();

//	@Column(name = "fk_review")
//	private long fkReview;


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

	public List<ImageFile> getFiles() {
		return this.files;
	}

	public void setFiles(final List<ImageFile> files) {
		this.files = files;
	}

//	public long getFkReview() {
//		return fkReview;
//	}
//
//	public void setFkReview(long idReview) {
//		this.fkReview = idReview;
//	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Location)) {
			return false;
		}
		Location other = (Location) obj;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (name != null && !name.trim().isEmpty())
			result += "name: " + name;
		if (path != null && !path.trim().isEmpty())
			result += ", path: " + path;
		if (files != null)
			result += ", files: " + files;
		return result;
	}
}