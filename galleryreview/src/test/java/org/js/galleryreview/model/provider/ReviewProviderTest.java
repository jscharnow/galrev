package org.js.galleryreview.model.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.js.galleryreview.model.entities.ImageFile;
import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.model.entities.Review;
import org.junit.Before;
import org.junit.Test;

public class ReviewProviderTest {

	private IReviewProvider provider;

	@Before
	public void setUp() {
		ReviewProvider.setPersistenceUnit("grev_localtest");
		provider = ReviewProvider.getInstance();
		provider.clearAll();
	}

	@Test
	public void testGetReviews() {
		assertTrue(provider.getReviews().isEmpty());
		Review rev = provider.createNewReview();
		assertNotNull(rev);
		String testName = "tstrev";
		rev.setName(testName);
		provider.mergeReview(rev);

		List<Review> reviews = provider.getReviews();
		assertFalse(reviews.isEmpty());
		assertEquals(1, reviews.size());
		Review receivedRev = reviews.get(0);
		assertEquals(testName, receivedRev.getName());
	}

	@Test
	public void testGetLatestReview() {
		assertLatestReview(null);
		Review rev = provider.createNewReview();
		String testName1 = "tstrev1";
		rev.setName(testName1);
		rev.setStartDate(createDate(2014, 02, 01));
		provider.mergeReview(rev);

		assertLatestReview(testName1);
		rev = provider.createNewReview();
		String testName2 = "tstrev2";
		rev.setName(testName2);
		rev.setStartDate(createDate(2014, 03, 01));
		provider.mergeReview(rev);
		assertLatestReview(testName2);

	}

	private void assertLatestReview(String name) {
		Review rev = provider.getLatestReview();
		if (null == name) {
			assertNull(rev);
		} else {
			assertNotNull(rev);
			assertEquals(name, rev.getName());
		}
	}

	private Date createDate(int year, int month, int day) {
		LocalDate ld = LocalDate.of(year, month, day);
		return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	@Test
	public void testFiles() {
		Review rev = provider.createNewReview();
		rev.setName("review");
		rev.setStartDate(new Date());
		rev=provider.mergeReview(rev);

		Location location = new Location();
		String locPath = "locPath";
		location.setPath(locPath);
		location.setName("Testlocation");
//		location.setFkReview(rev.getId());
		location=provider.mergeLocation(location);
		rev.getLocations().add(location);
		rev=provider.mergeReview(rev);
		
		ImageFile file = new ImageFile();
		String path = "path";
		file.setPath(path);
		file.setFkLocation(location.getId());
		List<ImageFile> files = new ArrayList<ImageFile>();
		files.add(file);
		location.setFiles(files);
		file=provider.mergeFile(file);
		provider.mergeReview(rev);

		ImageFile fileRec = testSingleFile(path);
		String path2 = "path2";

		fileRec.setPath(path2);

		fileRec = provider.mergeFile(fileRec);

		testSingleFile(path2);
	}

	private ImageFile testSingleFile(String path) {
		Review rev;
		rev = provider.getLatestReview();
		List<Location> locations = rev.getLocations();
		assertNotNull(locations);
		assertEquals(1, locations.size());
		Location location = locations.get(0);
		List<ImageFile> filesRec = location.getFiles();
		assertFalse(filesRec.isEmpty());
		ImageFile fileRec = filesRec.get(0);
		assertEquals(path, fileRec.getPath());
		return fileRec;
	}

}
