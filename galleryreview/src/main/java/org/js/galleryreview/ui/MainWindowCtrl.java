package org.js.galleryreview.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import org.js.galleryreview.model.entities.Location;
import org.js.galleryreview.model.entities.Review;
import org.js.galleryreview.model.provider.ReviewProvider;
import org.js.galleryreview.ui.i18n.Texts;
import org.js.galleryreview.ui.obj.NavEntryType;
import org.js.galleryreview.ui.obj.NavTreeEntry;
import org.js.galleryreview.ui.work.LocationReaderWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindowCtrl {

	public static InputStream getFXMLStream() {
		return MainWindowCtrl.class.getResourceAsStream("mainwindow.fxml");
	}
	
	private static final int WAIT_TIME = 5000;

	@FXML
	private TreeTableView<NavTreeEntry> ttvRepository;

	@FXML
	private TreeTableColumn<NavTreeEntry, String> tcNavigation;

	@FXML
	private TreeTableColumn<?, ?> tcNavState;

	@FXML
	private Button btnPrev;

	@FXML
	private Button btnNext;

	@FXML
	private ImageView ivMainImage;

	@FXML
	private Button btnConfirm;

	@FXML
	private Button btnDel;
	
	@FXML
	private Text txtCurrentFileName;
	@FXML
	private Label lblReviewName;
	
	private TreeItem<NavTreeEntry> tiLocations;
	
	private Queue<LocationReaderWorker> workerQueue = new LinkedList<LocationReaderWorker>();
	private boolean running = true;
	
	private Object workerThreadSyncObject = new Object();

	private Review review;
	private ObjectProperty<TreeItem<NavTreeEntry>> displayedEntryProperty = new SimpleObjectProperty<TreeItem<NavTreeEntry>>();
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Adds the location to the navigation tree and and adds location to work queue.
	 *
	 * @param tiLocations the ti locations
	 * @param loc the loc
	 */
	private void addLocationToTree(TreeItem<NavTreeEntry> tiLocations,
			Location loc) {
		NavTreeEntry nteLoc = new NavTreeEntry(NavEntryType.LOCATION);
		nteLoc.setLocation(loc);
		TreeItem<NavTreeEntry> tiLocation = new TreeItem<NavTreeEntry>(nteLoc);
		tiLocations.getChildren().add(tiLocation);
		addWork(tiLocation);
	}
	
    private void addWork(TreeItem<NavTreeEntry> treeItem) {
		synchronized (workerThreadSyncObject) {
			workerQueue.add(new LocationReaderWorker(treeItem));
		}
	}

    @FXML
    void confirmInvoked(ActionEvent event) {

    }

	@FXML
    void deleteInvoked(ActionEvent event) {
		if (displayedEntryProperty.get() != null){
			removeEntry(tiLocations, displayedEntryProperty.get());
			// TODO: Set del flag and add to deleted entries
			displayedEntryProperty.set(null);
		}
    }

	private void removeEntry(TreeItem<NavTreeEntry> root, TreeItem<NavTreeEntry> toDelete) {
		if (root.getChildren().contains(toDelete)){
			root.getChildren().remove(toDelete);
		}else{
			for (TreeItem<NavTreeEntry> entry: root.getChildren()){
				removeEntry(entry, toDelete);
			}
		}
	}

	private Image getBrokenImage() {
		// TODO: Get Broken image from resources
		return null;
	}

	private ReviewProvider getProvider() {
		return ReviewProvider.getInstance();
	}

	@FXML
	void initialize() {
		assert ttvRepository != null : "fx:id=\"ttvRepository\" was not injected: check your FXML file 'mainwindow.fxml'.";
		assert btnPrev != null : "fx:id=\"btnPrev\" was not injected: check your FXML file 'mainwindow.fxml'.";
		assert btnNext != null : "fx:id=\"btnNext\" was not injected: check your FXML file 'mainwindow.fxml'.";
		assert ivMainImage != null : "fx:id=\"ivMainImage\" was not injected: check your FXML file 'mainwindow.fxml'.";
		assert btnConfirm != null : "fx:id=\"btnConfirm\" was not injected: check your FXML file 'mainwindow.fxml'.";
		assert btnDel != null : "fx:id=\"btnDel\" was not injected: check your FXML file 'mainwindow.fxml'.";

		initTree();
		
		initWorker();
		
		ttvRepository.getParent().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			switch( event.getCode()){
			case DELETE:
				deleteInvoked(null);
			default:
				break;
			}
		});

		displayedEntryProperty.addListener((ChangeListener<TreeItem<NavTreeEntry>>) (
				observable, oldValue, newValue) -> {
			if (null == newValue) {
				setImage(null);
			} else {
				NavTreeEntry nte = newValue.getValue();
				File file = new File(nte.getDirectoryPath(), nte
						.getFileName());
				setImage(file);
			}
		});
	}

	private void initTree() {
		TreeItem<NavTreeEntry> tiReview = new TreeItem<NavTreeEntry>(
				new NavTreeEntry(NavEntryType.ROOT_REVIEW));
		tiLocations = new TreeItem<NavTreeEntry>(
				new NavTreeEntry(NavEntryType.LOCATIONS));
		TreeItem<NavTreeEntry> tiToDelete = new TreeItem<NavTreeEntry>(
				new NavTreeEntry(NavEntryType.TO_DELETE));

		Callback<CellDataFeatures<NavTreeEntry, String>, ObservableValue<String>> fct = new TreeItemPropertyValueFactory<NavTreeEntry, String>(
				"identification");

		tcNavigation.setCellValueFactory(fct);

		Callback<TreeTableColumn<NavTreeEntry, String>, TreeTableCell<NavTreeEntry, String>> cellFactory = new Callback<TreeTableColumn<NavTreeEntry, String>, TreeTableCell<NavTreeEntry, String>>() {

			@Override
			public TreeTableCell<NavTreeEntry, String> call(
					TreeTableColumn<NavTreeEntry, String> param) {
				TreeTableCell<NavTreeEntry, String> cell = new TextFieldTreeTableCell<NavTreeEntry, String>(){
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						NavTreeEntry navEntry = getTreeTableRow().getItem();
						Tooltip ttip = null;
						if (null != navEntry){
							if (NavEntryType.LOCATION == navEntry.getType()){
								ttip=getTooltip();
								if (null == ttip){
									ttip = new Tooltip();
									setTooltip(ttip);
								}
								ttip.setText(navEntry.getLocation().getPath());
							}
						}
						setTooltip(ttip);
					}
				};
				return cell;
			}

		};
		tcNavigation.setCellFactory(cellFactory);

		ttvRepository.setRoot(tiReview);
		tiReview.getChildren().add(tiLocations);
		tiReview.getChildren().add(tiToDelete);

		tiReview.expandedProperty().set(true);

		review = getProvider().getLatestReview();
		if (null == review) {
			// TODO: Error and create review dialog
			throw new RuntimeException("Cannot start without review");
		}
		lblReviewName.setText(review.getName());
		for (Location loc : review.getValidLocations()) {
			addLocationToTree(tiLocations, loc);
		}

		ttvRepository
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(ChangeListener<TreeItem<NavTreeEntry>>) (observable,
								oldValue, newValue) -> {
									if (null != newValue){
										nteSelected(newValue);
									}
						});
		
		ContextMenu menu = new ContextMenu();
		ttvRepository.setContextMenu(menu);
		MenuItem miNewLocation = new MenuItem(Texts.getText("miAddLocation"));
		menu.getItems().add(miNewLocation);
		MenuItem miRemoveLocation = new MenuItem(Texts.getText("miRemoveLocation"));
		menu.getItems().add(miRemoveLocation);

		menu.setOnShowing((WindowEvent wev) -> {
			TreeItem<NavTreeEntry> selectedItem = ttvRepository
					.getSelectionModel().getSelectedItem();
			miNewLocation.setDisable((tiLocations != selectedItem));
			boolean isLocation = selectedItem != null && selectedItem.getValue().getType() == NavEntryType.LOCATION;
			miRemoveLocation.setDisable(!isLocation);
		});

		miRemoveLocation.setOnAction((ActionEvent e) -> {
			// TODO: confirm dialog
			TreeItem<NavTreeEntry> selectedItem = ttvRepository
					.getSelectionModel().getSelectedItem();
			selectedItem.getParent().getChildren().remove(selectedItem);
			Location loc = selectedItem.getValue().getLocation();
			loc.setValid(false);
			getProvider().mergeLocation(loc);
		});

		miNewLocation.setOnAction((ActionEvent e) -> {
			DirectoryChooser diLocation = new DirectoryChooser();
			File dir = diLocation.showDialog(ttvRepository.getScene()
					.getWindow());
			if (null != dir) {
				Location loc = new Location();
				review.getLocations().add(loc);
				loc.setPath(dir.getAbsolutePath());
				loc.setName(dir.getName());
				getProvider().mergeReview(review);
				addLocationToTree(tiLocations, loc);
			}
		});
		
	}

	private void initWorker() {
		Runnable r = new Runnable(){
			private LocationReaderWorker currentWork;

			public void run(){
				while (running){
					logger.trace("Task thread checking for work. Current: " + currentWork+", queue: " + workerQueue.size());
					if (null == currentWork || currentWork.isFinished()){
						LocationReaderWorker work;
						synchronized (workerThreadSyncObject) {
							work=workerQueue.poll();
						}
						if (null != work){
							currentWork = work;
							new Thread(work).start();
						}
					}
					synchronized (workerThreadSyncObject) {
						try {
							workerThreadSyncObject.wait(WAIT_TIME);
						} catch (InterruptedException e) {
						}						
					}
				}
				synchronized (workerThreadSyncObject) {
					workerThreadSyncObject.notify();
				}
			}
		};
		new Thread(r, "ReadWorker").start();
	}

	private void nteSelected(TreeItem<NavTreeEntry> newValue) {
		logger.debug("NTE selected: " + newValue);
		if (newValue != null && newValue.getValue() != null && newValue.getValue().getType() == NavEntryType.FILE){
			displayedEntryProperty.set(newValue);
		}else{
			displayedEntryProperty.set(null);
		}
	}

	private void setImage(File file) {
		String name="";
		Image image = null;
		if (null != file){
			name = file.getName();
			if (file.exists()){
				try {
					image = new Image(new FileInputStream(file));
					ivMainImage.setImage(image);
					double height;
					double ratio=image.getWidth()/image.getHeight();
					height = ivMainImage.getFitWidth() * ratio;
					ivMainImage.setFitHeight(height);
				} catch (FileNotFoundException e) {
					image = getBrokenImage();
				}
			}else{
				image = getBrokenImage();
			}
		}
		ivMainImage.setImage(image);
		txtCurrentFileName.setText(name);
	}

	public void terminate() {
		running = false;
		try {
			synchronized (workerThreadSyncObject) {
				workerThreadSyncObject.notify();
				workerThreadSyncObject.wait(5000);
			}
		} catch (InterruptedException e) {
		}
	}

}
