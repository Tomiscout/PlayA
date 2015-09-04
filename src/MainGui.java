import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainGui extends HBox {

	static Slider seekBar;
	static SongTable table;
	static Label songLabel = null;
	private static boolean isSeeking = false;
	private boolean isShrinked = false;

	private static Stage downloaderStage;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MainGui() {

		getStylesheets().add("MainTheme.css");

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

		// Adds center pane
		VBox centerPane = new VBox();
		centerPane.setMaxHeight(Double.MAX_VALUE);
		centerPane.setSpacing(4);

		// Ads right pane
		VBox rightPane = new VBox();

		VBox topPane = new VBox();

		Button ytbDownloaderBtn = new Button("Open Youtube downloader");
		ytbDownloaderBtn.setOnAction(e -> {
			displayDownloader();
		});

		// Playing options
		ObservableList<String> options = FXCollections.observableArrayList("Normal", "Shuffle");
		final ComboBox comboBox = new ComboBox(options);
		comboBox.setValue("Shuffle");
		comboBox.valueProperty().addListener((ov, s, s1) -> {
			PlaylistController.setPlayingMode((String) s1);
		});

		songLabel = new Label("Sng name");
		songLabel.setFont(new Font("Impact", 18));

		// Adds top Player pane
		HBox playerPane = new HBox();
		playerPane.setSpacing(2);

		// Slider
		seekBar = new Slider();
		seekBar.setPrefWidth(256);
		seekBar.setMin(0);
		seekBar.setMax(100);
		seekBar.setValue(1.0);
		seekBar.setPadding(new Insets(5, 0, 4, 0));

		seekBar.setOnMousePressed(e -> {
			isSeeking = true;
		});
		seekBar.setOnMouseClicked(m -> {
			FXMediaPlayer.seek(seekBar.getValue());
			isSeeking = false;
		});
		seekBar.setOnMouseReleased(e -> isSeeking = false);

		Slider volumeBar = new Slider();
		volumeBar.setMax(1);
		volumeBar.setValue(1);
		volumeBar.setPrefWidth(100);
		volumeBar.setPadding(new Insets(5, 0, 4, 0));
		volumeBar.valueProperty().addListener(e -> {
			FXMediaPlayer.setVolume(volumeBar.getValue());
		});

		// Play
		Button playBtn = new Button();
		playBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("play.png")));
		playBtn.setTooltip(new Tooltip("Play"));
		playBtn.setOnAction(e -> PlaySelectedSong());

		// Pause
		Button pauseBtn = new Button();
		pauseBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("pause.png")));
		pauseBtn.setTooltip(new Tooltip("Pause"));
		pauseBtn.setOnAction(e -> {
			PlayerController.pause();
		});

		// Stop
		Button stopBtn = new Button();
		stopBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("stop.png")));
		stopBtn.setTooltip(new Tooltip("Stop"));
		stopBtn.setOnAction(e -> {
			PlayerController.stop();
		});

		// Next
		Button nextBtn = new Button();
		nextBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("next.png")));
		nextBtn.setTooltip(new Tooltip("Next"));
		nextBtn.setOnAction(e -> {
			PlaylistController.playNextSong();
		});

		// Previous
		Button previousBtn = new Button();
		previousBtn.setGraphic(new ImageView(FileUtils.getAssetsImage("previous.png")));
		previousBtn.setTooltip(new Tooltip("Previous"));
		previousBtn.setOnAction(e -> {
			PlaylistController.playPreviousSong();
		});

		//
		Button shrinkBtn = new Button("卐");
		shrinkBtn.setOnAction(e -> {
			rightPane.setManaged(isShrinked);
			rightPane.setVisible(isShrinked);

			if (isShrinked) {
				shrinkBtn.setText("卐");
				Main.pStage.setWidth(Main.pStage.getWidth() + rightPane.getWidth());
			} else {
				shrinkBtn.setText("卍");
				Main.pStage.setWidth(Main.pStage.getWidth() - rightPane.getWidth());
			}

			isShrinked = !isShrinked;
		});

		playerPane.getChildren().addAll(playBtn, pauseBtn, stopBtn, nextBtn, previousBtn, seekBar, volumeBar,
				shrinkBtn);

		topPane.getChildren().addAll(songLabel, playerPane);

		centerPane.getChildren().addAll(topPane, table);

		PlaylistPane playlistPane = new PlaylistPane();

		rightPane.getChildren().addAll(comboBox, playlistPane, ytbDownloaderBtn);

		getChildren().addAll(centerPane, rightPane);
	}

	private static SongObject GetSelectedSong() {
		SongObject item = (SongObject) table.getSelectionModel().getSelectedItem();
		return item;
	}

	@SuppressWarnings("unchecked")
	private static ObservableList<SongObject> GetSelectedSongs() {
		return table.getSelectionModel().getSelectedItems();
	}

	private static void PlaySelectedSong() {
		SongObject selected = GetSelectedSong();
		if (selected != null) {
			PlaylistController.playSongFilename(PlaylistController.getSongFilepath(selected.getName()));
		}
	}

	public static void setSongName(String s) {
		songLabel.setText(s);
	}

	public static void setSeekValue(double d) {
		if (!isSeeking)
			seekBar.setValue(d);
	}

	private static void deleteSongFromPlaylist(SongObject po) {
		PlaylistWriter.removeLineFromFile(PlaylistWriter.getPlaylistFile(po.getPlaylist()),
				po.getFile().getAbsolutePath());
		table.getData().remove(po);
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
				ObservableList<SongObject> list = GetSelectedSongs();
				if (list.size() > 1) {
					alert.setContentText("Do you want to delete " + list.size() + "songs from playlist?");
				} else if (!list.isEmpty()) {
					alert.setContentText("Do you want to delete " + list.get(0).getName() + " from playlist?");
				}

				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.OK) {
					deleteSongFromPlaylist(GetSelectedSong());
				}
			});

			MenuItem itemDeleteFile = new MenuItem("Delete File");
			itemDeleteFile.setOnAction(e -> {
				ObservableList<SongObject> list = GetSelectedSongs();
				if (list.size() > 1) {
					alert.setContentText("Do you want to delete " + list.size() + "songs?");
				} else if (!list.isEmpty()) {
					alert.setContentText("Do you want to delete " + list.get(0).getName() + " ?");
				}
				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.OK) {
					for (SongObject so : list) {
						try {
							File songFile = so.getFile();
							if (FXMediaPlayer.getCurrentSong().equals(so.getFile()))
								FXMediaPlayer.dispose();
							Files.delete(songFile.toPath());
						} catch (IOException ioe) {
							System.out.println("File is already in use, can't delete!");
							so.getFile().deleteOnExit();
						}
						deleteSongFromPlaylist(so);
						System.out.println("Deleted " + so.getFile().getAbsolutePath());
					}
				}
			});

			getItems().addAll(itemOpen, itemContainingFolder, itemDelete, itemDeleteFile);
		}
	}

	public static void displayDownloader() {
		downloaderStage = new Stage();
		YoutubeDownloaderUI ui = new YoutubeDownloaderUI();
		Scene downloaderScene = new Scene(ui, 512,512);
		
		downloaderStage.setScene(downloaderScene);
		downloaderStage.show();
	}
}
