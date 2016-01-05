package main.tomiscout.ui;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSlider.IndicatorPosition;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import main.tomiscout.PlayA;
import main.tomiscout.playlistControl.PlaylistController;
import main.tomiscout.playlistControl.PlaylistWriter;
import main.tomiscout.ui.playlist.PlaylistPane;
import main.tomiscout.utils.DataUtils;
import main.tomiscout.utils.FileUtils;

public class MainGui extends BorderPane {

	static PlaylistPane playlistPane;
	static JFXSliderCustom seekBar;
	public static SongTable table;
	static Label songLabel = null;
	static Label songLengthLabel = null;
	static ImageView albumCover;
	static ImageView songBackground;
	private static boolean isSeeking = false;
	private static boolean searchMode = false;
	private static int shrinkMode = 0;
	private static ImageView playImage;
	private static ImageView pauseImage;
	private static ImageView shrinkIn;
	private static ImageView shrinkOut;
	private static ImageView shrinkLeft;
	private static ImageView shuffleOn;
	private static ImageView shuffleOff;
	private static ImageView repeatOn;
	private static ImageView repeatOff;
	private static Button playBtn;
	private static Button shrinkBtn;
	private static Button shuffleBtn;
	private static Button repeatBtn;
	private static ObservableList<PlaylistWriter.SongObject> searchList = FXCollections.observableArrayList();

	public MainGui() {
		getStylesheets().add("main/tomiscout/resources/css/Vintage.css");

		SongContextMenu songContext = new SongContextMenu();

		// TableView
		table = new SongTable();
		table.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.SECONDARY && !table.getSelectionModel().getSelectedItems().isEmpty()) {
				songContext.show(table, e.getScreenX(), e.getScreenY());
			} else {
				songContext.hide();
			}
		});

		// Slider
		songLengthLabel = new Label("00:00");
		songLengthLabel.setFont(new Font(12));
		BorderPane lengthLabelPane = new BorderPane();
		lengthLabelPane.setPadding(new Insets(-12, 4, 0, 0));
		lengthLabelPane.setRight(songLengthLabel);

		seekBar = new JFXSliderCustom();
		seekBar.setValue(1.0);
		seekBar.setMinWidth(400);
		seekBar.setIndicatorPosition(IndicatorPosition.LEFT);

		seekBar.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				isSeeking = true;
			}
		});
		seekBar.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				isSeeking = false;
			}
		});

		seekBar.setOnMouseClicked(m -> {
			FXMediaPlayer.seek(seekBar.getValue());
			isSeeking = false;
		});

		StackPane songSeekPane = new StackPane();
		songSeekPane.getChildren().addAll(lengthLabelPane, seekBar);

		JFXSlider volumeBar = new JFXSlider();
		volumeBar.setMax(100);
		volumeBar.setValue(100);
		volumeBar.setMinWidth(100);
		volumeBar.valueProperty().addListener(e -> {
			FXMediaPlayer.setVolume(volumeBar.getValue() / 100);
		});
		volumeBar.setIndicatorPosition(IndicatorPosition.LEFT);

		playImage = new ImageView(FileUtils.getAssetsImage("play.png"));
		pauseImage = new ImageView(FileUtils.getAssetsImage("pause.png"));
		shrinkIn = new ImageView(FileUtils.getAssetsImage("shrinkIn.png"));
		shrinkOut = new ImageView(FileUtils.getAssetsImage("shrinkOut.png"));
		shrinkLeft = new ImageView(FileUtils.getAssetsImage("shrinkLeft.png"));
		shuffleOn = new ImageView(FileUtils.getAssetsImage("shuffleOn.png"));
		shuffleOff = new ImageView(FileUtils.getAssetsImage("shuffleOff.png"));
		repeatOn = new ImageView(FileUtils.getAssetsImage("repeatOn.png"));
		repeatOff = new ImageView(FileUtils.getAssetsImage("repeatOff.png"));

		// Play
		playBtn = new Button();
		playBtn.setGraphic(playImage);
		playBtn.setTooltip(new Tooltip("Play"));
		playBtn.setOnAction(e -> FXMediaPlayer.togglePlay());

		// Next
		Button nextBtn = new Button();
		nextBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("nextTrack.png")));
		nextBtn.setTooltip(new Tooltip("Next"));
		nextBtn.setOnAction(e -> {
			PlaylistController.playNextSong();
		});

		// Previous
		Button previousBtn = new Button();
		previousBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("previousTrack.png")));
		previousBtn.setTooltip(new Tooltip("Previous"));
		previousBtn.setOnAction(e -> {
			PlaylistController.playPreviousSong();
		});

		shuffleBtn = new Button();
		shuffleBtn.setGraphic(shuffleOn);
		shuffleBtn.setTooltip(new Tooltip("Shuffle"));
		shuffleBtn.setOnAction(e -> {
			if (PlaylistController.toggleShuffle()) {
				shuffleBtn.setGraphic(shuffleOn);
			} else {
				shuffleBtn.setGraphic(shuffleOff);
			}
		});

		repeatBtn = new Button();
		repeatBtn.setGraphic(repeatOff);
		repeatBtn.setTooltip(new Tooltip("Repeat"));
		repeatBtn.setOnAction(e -> {
			if (PlaylistController.toggleRepeat()) {
				repeatBtn.setGraphic(repeatOn);
			} else {
				repeatBtn.setGraphic(repeatOff);
			}
		});
		TextField searchField = new TextField();
		searchField.setVisible(false);
		searchField.setManaged(false);
		searchField.setDisable(true);
		searchField.setOnKeyPressed(key -> {
			Platform.runLater(new Runnable() {
				public void run() {
					searchForSong(searchField.getText());
				}
			});
		});

		Button searchBtn = new Button();
		searchBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("search.png")));
		searchBtn.setOnAction(a -> {
			if (searchMode) {
				turnOffSearch();
				searchField.setVisible(false);
				searchField.setManaged(false);
				searchField.setDisable(true);
				searchMode = false;
			} else {
				searchField.setVisible(true);
				searchField.setManaged(true);
				searchField.setDisable(false);
				searchField.requestFocus();
				searchMode = true;
			}
		});
		/*
		 * Button settingsBtn = new Button(); settingsBtn.setGraphic(new
		 * ImageView(FileUtils.getAssetsImage("settings.png")));
		 * settingsBtn.setOnAction(e -> {});
		 */
		shrinkBtn = new Button();
		shrinkBtn.setGraphic(shrinkLeft);
		shrinkBtn.setOnAction(e -> changeShrinkMode());

		songLabel = new Label("Hello!!");
		songLabel.setFont(new Font("Impact", 21));
		songLabel.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				if (e.getClickCount() == 2) {
					scrollToFile(PlaylistController.currentSong);
				}
			}
		});// scrolls to song if doubleclicked song label

		Reflection reflection = new Reflection();
		reflection.setFraction(0.2);

		albumCover = new ImageView();
		albumCover.setFitHeight(80);
		albumCover.setFitHeight(80);
		albumCover.setPreserveRatio(true);
		albumCover.setSmooth(true);
		//albumCover.setEffect(reflection);
		

		songBackground = new ImageView();

		HBox topPane = new HBox();
		VBox controlPane = new VBox();
		BorderPane seekPane = new BorderPane();
		StackPane buttonLayout = new StackPane();
		BorderPane songPane = new BorderPane();
		HBox buttonsPane = new HBox();
		HBox settingsPane = new HBox();

		buttonLayout.setAlignment(Pos.TOP_LEFT);
		controlPane.setPadding(new Insets(4));
		controlPane.setSpacing(4);
		seekPane.setPadding(new Insets(0, 4, 0, 4));
		buttonsPane.setSpacing(2);
		buttonsPane.setAlignment(Pos.CENTER_LEFT);
		settingsPane.setAlignment(Pos.BOTTOM_RIGHT);

		songPane.setLeft(buttonsPane);
		songPane.setRight(settingsPane);
		seekPane.setLeft(songSeekPane);
		seekPane.setRight(volumeBar);
		
		StackPane albumPane = new StackPane(albumCover);
		albumPane.setPadding(new Insets(4));

		controlPane.getChildren().addAll(songLabel, buttonLayout, seekPane);
		buttonLayout.getChildren().addAll(songBackground, songPane);
		settingsPane.getChildren().addAll(searchField, searchBtn, shrinkBtn);
		topPane.getChildren().addAll(controlPane, albumPane);
		topPane.setSpacing(4);

		buttonsPane.getChildren().addAll(previousBtn, playBtn, nextBtn, shuffleBtn, repeatBtn);

		playlistPane = new PlaylistPane();
		playlistPane.setPadding(new Insets(4));

		StackPane tablePane = new StackPane(table);
		tablePane.setPadding(new Insets(4));

		setCenter(tablePane);
		setRight(playlistPane);
		setTop(topPane);

		// TODO settings
		// TODO scrolling background and visuals
	}

	private static PlaylistWriter.SongObject GetSelectedSong() {
		PlaylistWriter.SongObject item = (PlaylistWriter.SongObject) table.getSelectionModel().getSelectedItem();
		return item;
	}

	private static ObservableList<PlaylistWriter.SongObject> GetSelectedSongs() {
		return table.getSelectionModel().getSelectedItems();
	}

	private static void PlaySelectedSong() {
		PlaylistWriter.SongObject selected = GetSelectedSong();
		if (selected != null) {
			PlaylistController.playSong(selected.getFile(), true);
		}
	}

	public static void changePlayImage(boolean b) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (playBtn != null && playImage != null && pauseImage != null) {
					if (b)
						playBtn.setGraphic(playImage);
					else
						playBtn.setGraphic(pauseImage);
				}
			}
		});
	}

	public static void changeShrinkMode() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				shrinkMode++;
				if (shrinkMode > 2)
					shrinkMode = 0;

				if (shrinkMode == 0) {
					playlistPane.setManaged(true);
					playlistPane.setVisible(true);
					table.setManaged(true);
					table.setVisible(true);

					shrinkBtn.setGraphic(shrinkLeft);
					PlayA.setHeight(PlayA.pStage.getHeight() + table.getHeight());
					PlayA.setWidth(PlayA.pStage.getWidth() + playlistPane.getWidth());
				} else if (shrinkMode == 1) { // Hide playlists
					playlistPane.setManaged(false);
					playlistPane.setVisible(false);

					shrinkBtn.setGraphic(shrinkIn);
					PlayA.setWidth(PlayA.pStage.getWidth() - playlistPane.getWidth());
				} else if (shrinkMode == 2) { // Compact
					table.setManaged(false);
					table.setVisible(false);

					PlayA.setHeight(PlayA.pStage.getHeight() - table.getHeight());
					shrinkBtn.setGraphic(shrinkOut);
				}
			}
		});
	}

	public static void setSongName(String s) {
		songLabel.setText(s);
	}

	public static void setSeekValue(double d) {
		if (!isSeeking)
			seekBar.setValue(d);
	}

	public static SongTable getSongTable() {
		return table;
	}

	// ContextMenu classes
	public class SongContextMenu extends ContextMenu {
		public SongContextMenu() {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Song deletion");
			alert.setHeaderText("Are you sure?");

			MenuItem itemOpen = new MenuItem("Play");
			itemOpen.setOnAction(e -> PlaySelectedSong());

			MenuItem itemContainingFolder = new MenuItem("Open containing folder");
			itemContainingFolder.setOnAction(e -> {
				File songFile = GetSelectedSong().getFile();
				if (songFile.exists()) {
					try {
						Desktop.getDesktop().open(songFile.getParentFile());
					} catch (Exception e1) {
						e1.printStackTrace();
						System.out.println("Can't open containing folder");
					}
				} else {
					System.out.println(songFile.getAbsolutePath() + " doesn't exist");
				}

			});

			MenuItem itemDelete = new MenuItem("Delete from playlist");
			itemDelete.setOnAction(e -> {
				ObservableList<PlaylistWriter.SongObject> list = GetSelectedSongs();
				if (list.size() > 1) {
					alert.setContentText("Do you want to delete " + list.size() + "songs from playlist?");
				} else if (!list.isEmpty()) {
					alert.setContentText("Do you want to delete " + list.get(0).getName() + " from playlist?");
				}

				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.OK) {
					PlaylistWriter.deleteSongFromPlaylist(GetSelectedSong());
				}
			});

			MenuItem itemDeleteFile = new MenuItem("Delete File");
			itemDeleteFile.setOnAction(e -> {
				ObservableList<PlaylistWriter.SongObject> list = GetSelectedSongs();
				if (list.size() > 1) {
					alert.setContentText("Do you want to delete " + list.size() + "songs?");
				} else if (!list.isEmpty()) {
					alert.setContentText("Do you want to delete " + list.get(0).getName() + " ?");
				}
				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.OK) {
					for (PlaylistWriter.SongObject so : list) {
						File songFile = so.getFile();
						File currentSong = FXMediaPlayer.getCurrentSong();
						if (currentSong != null && currentSong.equals(so.getFile()))
							FXMediaPlayer.dispose();

						if (!songFile.delete()) {
							System.out.println("File is already in use, can't delete!");
							so.getFile().deleteOnExit();
						}

						PlaylistWriter.deleteSongFromPlaylist(so);
						System.out.println("Deleted " + so.getFile().getAbsolutePath());
					}
				}
			});

			getItems().addAll(itemOpen, itemContainingFolder, itemDelete, itemDeleteFile);
		}
	}

	public static void scrollToFile(File song) {
		String name = FileUtils.truncateFileType(song.getName());
		int vIndex = -1;
		ObservableList<PlaylistWriter.SongObject> list = table.getData();
		for (int i = 0; i < list.size(); i++) {
			if (name.equals(list.get(i).getName())) {
				vIndex = i;
				break;
			}
		}
		if (vIndex >= 0) {

			table.scrollTo(vIndex);
			table.getSelectionModel().select(vIndex);
		}
	}

	public class JFXSliderCustom extends JFXSlider {
		@Override
		protected Skin<?> createDefaultSkin() {
			return new JFXSliderSkinCustom(this);
		}
	}

	public static void setAlbumArt(BufferedImage bi) {
		if (albumCover != null) {
			if (bi != null) {
				WritableImage wr = new WritableImage(bi.getWidth(), bi.getHeight());
				PixelWriter pw = wr.getPixelWriter();
				for (int x = 0; x < bi.getWidth(); x++) {
					for (int y = 0; y < bi.getHeight(); y++) {
						pw.setArgb(x, y, bi.getRGB(x, y));
					}
				}

				albumCover.setImage(wr);
				// songBackground.setImage(wr);
			} else {
				albumCover.setImage(null);
			}
		}
	}

	public static void searchForSong(String text) {
		searchList.clear();
		for (PlaylistWriter.SongObject so : table.getData()) {
			if (so.getName().toLowerCase().contains(text)) {
				searchList.add(so);
			}
		}
		table.setList(searchList);
	}

	public static void turnOffSearch() {
		table.resetList();
		searchList.clear();
	}

	public static void setSongLengthLabel(long length) {
		songLengthLabel.setText(DataUtils.formatSeconds(length, false));
	}
}
