package org.js.galleryreview.model.provider;

import java.util.List;

import javax.persistence.PersistenceException;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.model.entities.Review;

public interface IReviewProvider {

	public List<Review> getReviews();
	
	public Review getLatestReview();
	
	public Review createNewReview();
	
	public Review mergeReview(Review review) throws PersistenceException;

	/**
	 * Clear all data - for test purposes only.
	 */
	public void clearAll();

	public ImageFile mergeFile(ImageFile fileRec) throws PersistenceException;

	public Location mergeLocation(Location location);
	
}
