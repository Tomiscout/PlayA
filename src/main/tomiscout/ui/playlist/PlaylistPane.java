package main.tomiscout.ui.playlist;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.tomiscout.playlistControl.PlaylistController;
import main.tomiscout.playlistControl.PlaylistWriter;
import main.tomiscout.utils.FileUtils;

public class PlaylistPane extends BorderPane {

	static TableView<PlaylistObject> table;
	public static ObservableList<PlaylistObject> data = FXCollections.observableArrayList();

	static Stage PlaylistCreationStage;

	static File workingDir = PlaylistWriter.getWorkingDir();
	static ProgressBar playlistLoadingBar;
	static double currentProgress;
	static double maxProgress;
	static double itemProgress;
	static double maxItemProgress;

	static boolean isFinished;

	@SuppressWarnings("unchecked")
	public PlaylistPane() {
		PlaylistContextMenuLocal localContext = new PlaylistContextMenuLocal();

		// Table
		table = new TableView<PlaylistObject>(data);
		table.setEditable(false);
		table.setMaxWidth(260);
		table.setMaxHeight(800);
		table.setPrefHeight(800);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<PlaylistObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setMinWidth(80);
		nameColumn.setPrefWidth(140);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<PlaylistObject, Integer> countColumn = new TableColumn("Songs");
		countColumn.setMinWidth(26);
		countColumn.setPrefWidth(42);
		countColumn.setCellValueFactory(new PropertyValueFactory<>("songs"));

		TableColumn<PlaylistObject, String> timeColumn = new TableColumn("Length");
		timeColumn.setMinWidth(40);
		timeColumn.setPrefWidth(74);
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

		table.getColumns().addAll(nameColumn, countColumn, timeColumn);

		// Double click listener
		table.setRowFactory(tv -> {
			TableRow<PlaylistObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					PlaylistObject rowData = row.getItem();
					PlaylistController.openPlaylist(rowData.getName(), true);
				}
			});
			return row;
		});
		table.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.SECONDARY && !getCurrentSelectedItems().isEmpty()) {
				localContext.show(table, e.getScreenX(), e.getScreenY());
			} else {
				localContext.hide();
			}
		});

		// ProgressBar
		playlistLoadingBar = new ProgressBar(0);
		playlistLoadingBar.setMaxWidth(Double.MAX_VALUE);
		disableProgressBar();

		setPadding(new Insets(10, 0, 0, 10));
		setMinWidth(260);

		setOnDragOver(event -> {
			if (event.getGestureSource() != this) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});

		// Drag'n'drop
		setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			DataFormat df = DataFormat.lookupMimeType("java.file-list");
			ArrayList<File> buffer = (ArrayList<File>) db.getContent(df);
			handlePlaylistDrop(buffer);
			event.consume();
		});

		setPadding(new Insets(4));

		setCenter(table);
		setBottom(playlistLoadingBar);
		
		// Loading playlist
		reloadPlaylists();
	}

	// Loads playlists
	public static void reloadPlaylists() {
		if (data != null)
			data.clear();

		File[] playlists = FileUtils.getExcludedFiles(PlaylistWriter.getWorkingDir(), ".plp");
		for (File f : playlists) {
			PlaylistHeader header = new PlaylistHeader(f);
			if (header.getType() == 0) {
				data.add(header.getPlaylistObject());
			}
		}

	}

	@SuppressWarnings("unchecked")
	public static ObservableList<PlaylistObject> getCurrentSelectedItems() {
		return table.getSelectionModel().getSelectedItems();
	}

	public static PlaylistObject getCurrentSelectedItem() {
		return (PlaylistObject) table.getSelectionModel().getSelectedItem();
	}

	// Handles dragged files on PlaylistPane
	private void handlePlaylistDrop(ArrayList<File> fileList) {
		int folders = 0;
		File[] filesArray = fileList.toArray(new File[fileList.size()]);

		for (File f : filesArray) {
			if (f.isDirectory())
				folders++;
		}

		if (folders <= 1) {
			PlaylistCreationData data = displayPlaylistCreation(fileList.get(0).getName(), false);
			if (data.isFinished()) {
				PlaylistWriter.createPlaylist(data.getName(), filesArray);
			}
		} else {
			// Display options window
			PlaylistCreationData data = displayPlaylistCreation("", true);
			if (data.isFinished()) {
				if (data.getRadio()) {
					PlaylistWriter.createPlaylist(data.getName(), filesArray);
				} else {
					PlaylistWriter.createFolderPlaylists(filesArray);
				}
			}
		}

	}

	public static void removeItem(PlaylistObject po) {
		data.remove(po);
	}

	private class PlaylistCreationData {
		private String name;
		private boolean radio;
		private boolean isFinished;

		public PlaylistCreationData(String s, boolean b, boolean finished) {
			name = s;
			radio = b;
			isFinished = finished;
		}

		public String getName() {
			return name;
		}

		public boolean getRadio() {
			return radio;
		}

		public boolean isFinished() {
			return isFinished;
		}
	}

	// Opens custom playlist factory window
	private PlaylistCreationData displayPlaylistCreation(String name, boolean radio) {
		PlaylistCreationStage = new Stage();
		PlaylistCreationStage.initModality(Modality.APPLICATION_MODAL);
		PlaylistCreationStage.setTitle("Create a custom playlist");
		PlaylistCreationStage.setWidth(260);
		PlaylistCreationStage.setHeight(137);
		PlaylistCreationStage.setResizable(false);

		PlaylistCreationPane pane = new PlaylistCreationPane(name, radio);
		Scene scene = new Scene(pane);
		PlaylistCreationStage.setScene(scene);
		PlaylistCreationStage.showAndWait();
		return new PlaylistCreationData(pane.getResult(), pane.getRadio(), pane.isFinished());
	}

	public static void closePlaylistCreation() {
		if (PlaylistCreationStage != null) {
			PlaylistCreationStage.close();
		}
	}

	public static void enableProgressBar(double x) {
		playlistLoadingBar.setVisible(true);
		playlistLoadingBar.setManaged(true);
		maxProgress = x;
	}

	public static void setProgressBarItemMax(double i) {
		maxItemProgress = i;
	}

	public static void setProgressBarItemValue(double i) {
		itemProgress = i;
		double position = (double) (currentProgress / maxProgress + 1 / maxProgress * itemProgress / maxItemProgress);
		playlistLoadingBar.setProgress(position);
	}

	public static void setProgressBarValue(double x) {
		currentProgress = x;
		double position = currentProgress / maxProgress;
		playlistLoadingBar.setProgress(position);
	}

	public static void disableProgressBar() {
		playlistLoadingBar.setVisible(false);
		playlistLoadingBar.setManaged(false);
	}

	// ContextMenu class
	public class PlaylistContextMenuLocal extends ContextMenu {
		public PlaylistContextMenuLocal() {

			MenuItem itemOpen = new MenuItem("Open");
			itemOpen.setOnAction(e -> PlaylistController.OpenSelectedPlaylists());

			MenuItem itemDelete = new MenuItem("Delete");
			itemDelete.setOnAction(e -> PlaylistController.DeleteSelectedPlaylists());

			MenuItem itemRename = new MenuItem("Rename");
			itemRename.setOnAction(e -> PlaylistController.RenamePlaylist());

			MenuItem itemRescan = new MenuItem("Rescan");
			itemRescan.setOnAction(e -> PlaylistController.RescanSelectedPlaylists());

			getItems().addAll(itemOpen, itemDelete, itemRename, itemRescan);
		}
	}
		
	public static void RenamePlaylist() {
		PlaylistObject selectedObj = PlaylistPane.getCurrentSelectedItem();
		if (selectedObj != null) {
			File playlistFile = new File(workingDir.getAbsolutePath() + "\\" + selectedObj.getName() + ".plp");
			if (!playlistFile.exists())
				return;

			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Playlist name");
			dialog.setContentText("Enter playlist name: ");

			// Gets input result and loops if filename is not allowed
			boolean nameLoop = true;
			Optional<String> result = null;
			while (nameLoop) {
				nameLoop = false;
				result = dialog.showAndWait();
				if (result.isPresent()) {
					if (!FileUtils.isNameCorrect(result.get())) {
						nameLoop = true;
					}
				}
			}

			if (result.isPresent()) {
				playlistFile.renameTo(new File(playlistFile.getParent() + "\\" + result.get() + ".plp"));
				PlaylistPane.reloadPlaylists();
			}
		}
	}
	
	public static void RescanSelectedPlaylists() {
		ObservableList<PlaylistObject> list = PlaylistPane.getCurrentSelectedItems();
		System.out.println("Selected items: "+list.size());
		for (PlaylistObject po : list) {
			System.out.println("Rescanning "+po.getName()+"...");
			PlaylistWriter.rescanPlaylist(po);
		}
	}
	
	public static void OpenSelectedPlaylists() {
		ObservableList<PlaylistObject> list = getCurrentSelectedItems();
		String[] playlistArray = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			playlistArray[i] = list.get(i).getName();
		}
		PlaylistController.openPlaylist(playlistArray, true);
	}
	
	public static void DeleteSelectedPlaylists() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Playlist deletion");
		alert.setHeaderText("Are you sure?");
		ObservableList<PlaylistObject> list = PlaylistPane.getCurrentSelectedItems();
		PlaylistObject[] listArray = list.toArray(new PlaylistObject[list.size()]);

		if (list != null) {
			alert.setContentText("Do you want to delete " + list.size() + " playlist(s)?");
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {
				for (PlaylistObject po : listArray) {
					String s = po.getName();
					// Makes correct filename for deletion
					String fName;
					if (s.endsWith(".plp")) {
						fName = workingDir + "\\" + s;
					} else {
						fName = workingDir + "\\" + s + ".plp";
					}

					File delFile = new File(fName);
					if (delFile.exists()) {
						delFile.delete();
						System.out.println("Deleted playlist: " + s);
					} else {
						System.out.println("Couldn't find playlist for deletion: " + delFile.getAbsolutePath());
					}
					PlaylistPane.removeItem(po);// Deleting from the table
				}

			} else {
				return;
			}

		}
	}

	public static class PlaylistObject {

		private String name;
		private int songs;
		private String length;

		public PlaylistObject(String name, int songs, String length) {
			this.name = name;
			this.songs = songs;
			this.length = length;
		}

		public PlaylistObject() {
			this.name = "";
			this.songs = 0;
			this.length = "";
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getSongs() {
			return songs;
		}

		public void setSongs(int songs) {
			this.songs = songs;
		}

		public String getLength() {
			return length;
		}

		public void setLength(String length) {
			this.length = length;
		}
	}
}
