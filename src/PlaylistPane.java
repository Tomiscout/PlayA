import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

@SuppressWarnings("rawtypes")
public class PlaylistPane extends VBox {

	static PlaylistTable folderTable;
	static PlaylistTable customTable;

	static TabPane tablePane;
	static File workingDir = PlaylistWriter.getWorkingDir();
	static ProgressBar bar;
	static double currentProgress;
	static double maxProgress;
	static double itemProgress;
	static double maxItemProgress;

	static boolean isFinished;
	private int buttonWidth = 64;

	@SuppressWarnings("unchecked")
	public PlaylistPane() {
		tablePane = new TabPane();
		tablePane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab foldersTab = new Tab("Folders");
		Tab customTab = new Tab("Custom");

		folderTable = new PlaylistTable();
		customTable = new PlaylistTable();

		BorderPane folderTablePane = new BorderPane();

		folderTablePane.setCenter(tablePane);

		foldersTab.setContent(folderTable);
		customTab.setContent(customTable);

		tablePane.getTabs().addAll(foldersTab, customTab);
		
		// ProgressBar
		bar = new ProgressBar(0);
		bar.setMaxWidth(Double.MAX_VALUE);
		folderTablePane.setBottom(bar);
		disableProgressBar();

		BorderPane topPane = new BorderPane();

		// Buttons
		Button newPlaylist = new Button("New");
		Button customPlaylist = new Button("Custom");
		Button renamePlaylist = new Button("Rename");
		Button delPlaylist = new Button("Delete");

		newPlaylist.setOnAction(e -> {
			displayChooser();

		});
		customPlaylist.setOnAction(e -> {
			displayCustomFactory();
		});

		renamePlaylist.setOnAction(e -> {
			RenamePlaylist();
		});

		delPlaylist.setOnAction(e -> {
			DeletePlaylists();
		});

		HBox bottomButtons = new HBox();
		bottomButtons.getChildren().addAll(newPlaylist, customPlaylist, renamePlaylist, delPlaylist);
		topPane.setBottom(bottomButtons);

		newPlaylist.setPrefWidth(buttonWidth);
		customPlaylist.setPrefWidth(buttonWidth);
		delPlaylist.setPrefWidth(buttonWidth);

		setSpacing(5);
		setPadding(new Insets(10, 0, 0, 10));
		getChildren().addAll(topPane, tablePane);
		setMinWidth(260);
		// Loading playlist
		reloadPlaylists();
	}

	//Loads playlists, hardcoded
	public static void reloadPlaylists() {
		PlaylistTable[] pt = {folderTable, customTable};
		for(PlaylistTable t : pt){
			ObservableList<PlaylistObject> data = t.data;
			if (data != null)
				data.clear();
		}
		
			File[] playlists = FileUtils.getExcludedFiles(PlaylistWriter.getWorkingDir(), ".plp");
			for (File f : playlists) {
				PlaylistHeader header = new PlaylistHeader(f);
				if(header.getType() == 0){
					folderTable.data.add(header.getPlaylistObject());
				}else if(header.getType() == 1){
					
				}else if(header.getType() == 2){
					
				}
				
			}
		
	}
	
	public static void DeletePlaylists(){
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Playlist deletion");
		alert.setHeaderText("Are you sure?");
		ObservableList<PlaylistObject> list = getCurrentSelectedItems();
		PlaylistTable currentTable = getCurrentTable();

		if (list != null) {
			alert.setContentText("Do you want to delete " + list.size() + " playlist(s)?");

			for (PlaylistObject po : list) {
				String s = po.getName();
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
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

					currentTable.data.remove(po);// Deleting from the table
				}
			}
		}
	}

	public static void RenamePlaylist(){
		PlaylistObject selectedObj = getCurrentSelectedItem();
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
				reloadPlaylists();
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
					return customTable;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static ObservableList<PlaylistObject> getCurrentSelectedItems() {
		return getCurrentTable().getSelectionModel().getSelectedItems();
	}

	@SuppressWarnings("unchecked")
	public static PlaylistObject getCurrentSelectedItem() {
		return (PlaylistObject) getCurrentTable().getSelectionModel().getSelectedItem();
	}

	// Opens custom playlist factory window
	private void displayCustomFactory() {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Create a custom playlist");
		window.setWidth(260);
		window.setHeight(390);
		window.setResizable(false);

		Scene scene = new Scene(new CustomPlaylistPane());
		window.setScene(scene);
		window.showAndWait();
	}

	// Opens Playlist folder chooser (JTree in SwingNode)
	private void displayChooser() {
		Stage window = new Stage();

		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle("Choose folder(s)");
		window.setWidth(270);
		window.setHeight(386);
		window.setResizable(false);

		GridPane grid = new GridPane();

		grid.setAlignment(Pos.CENTER);

		HBox buttonPane = new HBox(32);
		buttonPane.setAlignment(Pos.CENTER);
		CheckBox subCheckBox = new CheckBox("Sub-folders");

		Button selectButton = new Button("Select");

		buttonPane.getChildren().addAll(subCheckBox, selectButton);

		// Creates file chooser
		FolderChooserTree fct = new FolderChooserTree();
		final SwingNode swingNode = new SwingNode();
		createSwingContent(swingNode, fct);

		selectButton.setOnAction(e -> {
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

			if (!fct.folders.isEmpty() && result.isPresent()) {
				String name = result.get();
				PlaylistWriter.createPlaylist(name, fct.folders, subCheckBox.isSelected());
				window.close();
			}
		});

		grid.add(swingNode, 0, 0);
		grid.add(buttonPane, 0, 1);

		Scene scene = new Scene(grid);
		window.setScene(scene);
		window.showAndWait();
	}

	private void createSwingContent(final SwingNode swingNode, JPanel jp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingNode.setContent(jp);
			}
		});
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

}
