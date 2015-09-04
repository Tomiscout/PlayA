import java.io.File;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlaylistPane extends VBox {

	static PlaylistTable folderTable;
	static PlaylistTable youtubeTable;

	static Stage PlaylistCreationStage;

	static TabPane tablePane;
	static File workingDir = PlaylistWriter.getWorkingDir();
	static ProgressBar bar;
	static double currentProgress;
	static double maxProgress;
	static double itemProgress;
	static double maxItemProgress;

	static boolean isFinished;
	
	@SuppressWarnings("unchecked")
	public PlaylistPane() {
		tablePane = new TabPane();
		tablePane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab foldersTab = new Tab("Folders");
		Tab youtubeTab = new Tab("Youtube");
		
		PlaylistContextMenuLocal localContext = new PlaylistContextMenuLocal();

		folderTable = new PlaylistTable();
		youtubeTable = new PlaylistTable();

		BorderPane folderTablePane = new BorderPane();
		VBox youtubeTablePane = new VBox();

		folderTablePane.setCenter(folderTable);

		foldersTab.setContent(folderTablePane);		
		youtubeTab.setContent(youtubeTablePane);

		tablePane.getTabs().addAll(foldersTab, youtubeTab);

		folderTable.setOnMousePressed(e -> {
			if(e.getButton() == MouseButton.SECONDARY && !getCurrentSelectedItems().isEmpty()){
				localContext.show(folderTable, e.getScreenX(), e.getScreenY());
			}else{
				localContext.hide();
			}
		});
		
		
		
		youtubeTablePane.getChildren().addAll(youtubeTable);
		
		// ProgressBar
		bar = new ProgressBar(0);
		bar.setMaxWidth(Double.MAX_VALUE);
		folderTablePane.setBottom(bar);
		disableProgressBar();

		BorderPane topPane = new BorderPane();

		setSpacing(5);
		setPadding(new Insets(10, 0, 0, 10));
		getChildren().addAll(topPane, tablePane);
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

		// Loading playlist
		reloadPlaylists();
	}

	// Loads playlists, hardcoded
	public static void reloadPlaylists() {
		PlaylistTable[] pt = { folderTable, youtubeTable };
		for (PlaylistTable t : pt) {
			ObservableList<PlaylistObject> data = t.data;
			if (data != null)
				data.clear();
		}

		File[] playlists = FileUtils.getExcludedFiles(PlaylistWriter.getWorkingDir(), ".plp");
		for (File f : playlists) {
			PlaylistHeader header = new PlaylistHeader(f);
			if (header.getType() == 0) {
				folderTable.data.add(header.getPlaylistObject());
			} else if (header.getType() == 1) {
				youtubeTable.data.add(header.getPlaylistObject());
			} else if (header.getType() == 2) {

			}

		}

	}

	public static PlaylistTable getCurrentTable() {
		ObservableList<Tab> tabs = tablePane.getTabs();
		for (Tab t : tabs) {
			if (t.isSelected()) {
				String name = t.getText();
				if (name.equals("Folders")) {
					return folderTable;
				} else if (name.equals("Custom")) {
					return youtubeTable;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static ObservableList<PlaylistObject> getCurrentSelectedItems() {
		return getCurrentTable().getSelectionModel().getSelectedItems();
	}

	public static PlaylistObject getCurrentSelectedItem() {
		return (PlaylistObject) getCurrentTable().getSelectionModel().getSelectedItem();
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
			if(data.isFinished()){
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
		bar.setVisible(true);
		bar.setManaged(true);
		maxProgress = x;
	}

	public static void setProgressBarItemMax(double i) {
		maxItemProgress = i;
	}

	public static void setProgressBarItemValue(double i) {
		itemProgress = i;
		double position = (double) (currentProgress / maxProgress + 1 / maxProgress * itemProgress / maxItemProgress);
		bar.setProgress(position);
	}

	public static void setProgressBarValue(double x) {
		currentProgress = x;
		double position = currentProgress / maxProgress;
		bar.setProgress(position);
	}

	public static void disableProgressBar() {
		bar.setVisible(false);
		bar.setManaged(false);
	}

	//ContextMenu class
	public class PlaylistContextMenuLocal extends ContextMenu{
		public PlaylistContextMenuLocal(){
			
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
	
}
