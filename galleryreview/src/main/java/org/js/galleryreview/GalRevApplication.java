package org.js.galleryreview;

import java.io.File;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.js.galleryreview.model.entities.Review;
import org.js.galleryreview.model.provider.ReviewProvider;
import org.js.galleryreview.ui.MainWindowCtrl;
import org.js.galleryreview.ui.i18n.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Gallery Review Application
 *
 */
public class GalRevApplication extends Application {

	private static final String APP_DATA_DIR_NAME = "galrev";
	private static final String DB_NAME = "galrev";
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		Application.launch(GalRevApplication.class, args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		logger.info("GalleryReview starting");
		ResourceBundle bundle = Texts.getAppBundle();

		initApplication();

		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		Parent root = loader.load(MainWindowCtrl.getFXMLStream());

		Scene scene = new Scene(root);

		stage.setTitle("Gallery Review"); // TODO: Version info
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				MainWindowCtrl ctrl = loader.getController();
				ctrl.terminate();
				logger.debug("GalleryReview terminated");
				System.exit(0);
			}
		});
		stage.show();
	}

	private void initApplication() {
		// TODO: Initialization:
		// * First installation: Wizard standalone/multiuser, Name of first
		// review, Create first review
		// * Set persistence unit depending on settings
		ReviewProvider
				.setPersistenceUnit(ReviewProvider.SINGLE_USER_PERSISTENCE_UNIT);
		String homeDir = System.getProperty("user.home");
		String winAppData = homeDir + File.separator + "Application Data";
		String appDir;
		if (new File(winAppData).exists()) {
			// windows
			appDir = winAppData + File.separator + APP_DATA_DIR_NAME;
		} else {
			// linux
			appDir = homeDir + File.separator + "." + APP_DATA_DIR_NAME;
		}
		String dataDir = appDir + File.separator + "db" + File.separator
				+ DB_NAME;
		File dataDirFile = new File(dataDir);
		if (!dataDirFile.exists()) {
			dataDirFile.mkdirs();
			ReviewProvider.setInitDatabase(true);
		}
		ReviewProvider.setDbPath(dataDir);
		ReviewProvider prov = ReviewProvider.getInstance();
		Review startReview = prov.getLatestReview();
		if (null == startReview) {
			startReview = prov.createNewReview();
			startReview.setName(Texts.getText("txtDefaultReviewName"));
			startReview.setStartDate(new Date());
			prov.mergeReview(startReview);
		}
	}

	public static void terminate() {
		System.exit(0);
	}
}
