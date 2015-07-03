import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@SuppressWarnings("rawtypes")
public class PlaylistPane extends VBox {

	TableView table;
	public static ObservableList<PlaylistObject> data = FXCollections.observableArrayList();

	static String workingDir = FileUtils.getWorkDirectory();
	static ProgressBar bar;
	static double currentProgress;
	static double maxProgress;
	static double itemProgress;
	static double maxItemProgress;

	static boolean isFinished;
	private int buttonWidth = 86;

	public PlaylistPane() {
		BorderPane tablePane = new BorderPane();
		table = new TableView();
		table.setEditable(false);
		table.setItems(data);
		table.setMaxWidth(260);
		table.setMaxHeight(800);
		table.setPrefHeight(800);
		tablePane.setCenter(table);

		TableColumn<PlaylistObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setMaxWidth(230);
		nameColumn.setMinWidth(80);
		nameColumn.setPrefWidth(150);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<PlaylistObject, Integer> countColumn = new TableColumn("Songs");
		countColumn.setMaxWidth(40);
		countColumn.setMinWidth(26);
		countColumn.setCellValueFactory(new PropertyValueFactory<>("songs"));

		TableColumn<PlaylistObject, String> timeColumn = new TableColumn("Length");
		timeColumn.setMaxWidth(70);
		timeColumn.setMinWidth(40);
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

		table.getColumns().addAll(nameColumn, countColumn, timeColumn);

		// Double click listener
		table.setRowFactory(tv -> {
			TableRow<PlaylistObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					PlaylistObject rowData = row.getItem();
					PlaylistController.openPlaylist(rowData.getName());
				}
			});
			return row;
		});

		// ProgressBar
		bar = new ProgressBar(0);
		bar.setMaxWidth(Double.MAX_VALUE);
		tablePane.setBottom(bar);
		disableProgressBar();

		BorderPane topPane = new BorderPane();
		
			// Buttons
			Button newPlaylist = new Button("New");
			Button customPlaylist = new Button("Custom");
			Button delPlaylist = new Button("Delete");

			newPlaylist.setOnAction(e -> {
				displayChooser();

			});
			customPlaylist.setOnAction(e -> System.out.println("Custom playlist"));

			delPlaylist.setOnAction(e -> {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Playlist deletion");
				alert.setHeaderText("Are you sure?");
				PlaylistObject po = (PlaylistObject) table.getSelectionModel().getSelectedItem();
				
				if(po != null){
				String s = po.getName();
				alert.setContentText("Do you want to delete playlist " + s + "?");

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

					data.remove(po);// Deleting from the table
				}
			}
			});

			HBox bottomButtons = new HBox();
			bottomButtons.getChildren().addAll(newPlaylist, customPlaylist, delPlaylist);
			topPane.setBottom(bottomButtons);
		
		newPlaylist.setPrefWidth(buttonWidth);
		customPlaylist.setPrefWidth(buttonWidth);
		delPlaylist.setPrefWidth(buttonWidth);
		
		setSpacing(5);
		setPadding(new Insets(10, 0, 0, 10));
		getChildren().addAll(topPane, tablePane);
		setMinWidth(260);
		// Loading playlist
		File[] playlists = FileUtils.getExcludedFiles(new File(FileUtils.getWorkDirectory()), ".plp");
		for (File f : playlists) {
			try {
				String name = f.getName().substring(0, f.getName().length() - 4);
				String length;
				try {
					length = FileUtils.formatSeconds(Integer.parseInt(FileUtils.getFirstLine(f)));
				} catch (NumberFormatException e) {
					System.out.println("Bad song length in .plp " + name);
					length = FileUtils.formatSeconds(0);
				}
				data.add(new PlaylistObject(name, FileUtils.countLines(f.getAbsolutePath()) - 1, length));

				System.out.println("Added playlist " + f.getName());
			} catch (IOException ioe) {
				System.out.println("Couldn't count songs in playlist: " + f.getAbsolutePath());
			}
		}
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
		BorderPane bp = new BorderPane(buttonPane);

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

	public ObservableList<PlaylistObject> getProduct() {
		return data;
	}
}
