package org.js.galleryreview.model.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.model.entities.Review;

/**
 * The Class ReviewProvider is the data access class to retrieve and store any 
 * review related data by accessing persistent storage (database). 
 * TODO: Add transaction and error handling
 */
public class ReviewProvider implements IReviewProvider {

	private static final String DEFAULT_PERSISTENCE_UNIT = "galrev";

	public static final String SINGLE_USER_PERSISTENCE_UNIT = "grev_singleuser";

	private static ReviewProvider instance = new ReviewProvider();

	private static String persistenceUnit = DEFAULT_PERSISTENCE_UNIT;

	private EntityManagerFactory emf;

	private static String dbPath;

	private static boolean initDatabase;

	private ReviewProvider() {
	}

	public static String getPersistenceUnit() {
		return persistenceUnit;
	}

	/**
	 * Sets the persistence unit. Has to be invoked before any access is made to the database
	 *
	 * @param persistenceUnit the new persistence unit
	 */
	public static void setPersistenceUnit(String persistenceUnit) {
		ReviewProvider.persistenceUnit = persistenceUnit;
		getInstance().emf = null;
	}
	
	/**
	 * Sets the path to the h2 db to be used. Has to be invoked before any access is made to the database
	 *
	 * @param dbPath the new db path
	 */
	public static void setDbPath(String dbPath) {
		ReviewProvider.dbPath = dbPath;
	}

	@Override
	public List<Review> getReviews() {
		EntityManager em = getEntityManager();
		TypedQuery<Review> q = em.createQuery("Select rev from Review rev",
				Review.class);
		return q.getResultList();
	}

	private EntityManager getEntityManager() {
		EntityManagerFactory emf2 = getEMFactory();

		EntityManager em = emf2.createEntityManager();
		return em;
	}

	private EntityManagerFactory getEMFactory() {
		if (null == emf) {
			Map<String,String> override = new HashMap<String,String>();
			if (null != dbPath){
				override.put("javax.persistence.jdbc.url","jdbc:h2:"+dbPath);
			}
			if (initDatabase){
				// TODO: Consider creating schema from install.sql
				override.put("hibernate.hbm2ddl.auto","create-drop");
			}
			emf=Persistence.createEntityManagerFactory(getPersistenceUnit(), override);
		}
		return emf;
	}

	@Override
	public Review getLatestReview() {
		TypedQuery<Review> q = getEntityManager().createQuery("Select rev from Review rev left join fetch rev.locations order by rev.startDate desc",
				Review.class);
		q.setMaxResults(1);
		Review result = null;
		List<Review> resList = q.getResultList();
		if (!resList.isEmpty()){
			result = resList.get(0);
		}
		return result;
	}

	@Override
	public Review createNewReview() {
		return new Review();
	}

	@Override
	public Review mergeReview(Review review) throws PersistenceException {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		if (null == review.getId()) {
			em.persist(review);
		} else {
			review = em.merge(review);
		}
		em.getTransaction().commit();
		return review;
	}
	
	@Override
	public ImageFile mergeFile(ImageFile imagefile) throws PersistenceException {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		if (null == imagefile.getId()) {
			em.persist(imagefile);
		} else {
			imagefile = em.merge(imagefile);
		}
		em.getTransaction().commit();
		return imagefile;
	}
	
	@Override
	public Location mergeLocation(Location location) throws PersistenceException {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		if (null == location.getId()) {
			em.persist(location);
		} else {
			location = em.merge(location);
		}
		em.getTransaction().commit();
		return location;
	}

	public static void setInitDatabase(boolean initDatabase) {
		ReviewProvider.initDatabase = initDatabase;
	}
	
	/**
	 * Gets the instance.
	 *
	 * @return the instance
	 */
	public static ReviewProvider getInstance() {
		return instance;
	}
	
	@Override
	public void clearAll() {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		List<Review> reviews = getReviews();
		for (Review review : reviews) {
			
			em.remove(em.find(Review.class, review.getId()));			
		}
		em.getTransaction().commit();
	}


}
