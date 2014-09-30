package org.js.galleryreview.model.entities;

import javax.persistence.Entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Transient;
import javax.persistence.Version;

import java.lang.Override;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.OneToMany;

import java.util.Date;
import java.util.stream.Collectors;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "review")
public class Review implements Serializable {

	/**
    * 
    */
	private static final long serialVersionUID = -6810222274302736835L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 100, name = "name")
	@NotNull
	private String name;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date startDate;

	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name = "fk_review")
	private List<Location> locations = new ArrayList<Location>();

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

	@Transient
	public List<Location> getValidLocations(){
		List<Location> allLocations = new ArrayList<Location>(getLocations());
		return allLocations.stream().filter(x -> x.isValid()).collect(Collectors.toList());
	}

	public List<Location> getLocations() {
		return this.locations;
	}

	public void setLocations(final List<Location> locations) {
		this.locations = locations;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Review)) {
			return false;
		}
		Review other = (Review) obj;
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (id != null)
			result += "id: " + id;
		result += ", version: " + version;
		if (name != null && !name.trim().isEmpty())
			result += ", name: " + name;
		if (locations!= null && !locations.isEmpty())
			result += ", locations: " + locations;
		if (startDate != null)
			result += ", startDate: " + startDate;
		return result;
	}
}