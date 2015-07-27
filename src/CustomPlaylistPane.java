import java.util.ArrayList;
import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CustomPlaylistPane extends BorderPane {


	TableView table;

	public CustomPlaylistPane() {
		getStylesheets().add("MainTheme.css");
		table = new TableView();
		table.setEditable(false);
		
		table.setMaxWidth(260);
		table.setMaxHeight(390);
		table.setPrefHeight(400);
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<PlaylistObject, Boolean> selectedColumn = new TableColumn("");
		selectedColumn.setResizable(false);
		selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));

		TableColumn<PlaylistObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setMaxWidth(230);
		nameColumn.setMinWidth(80);
		nameColumn.setPrefWidth(142);
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

		HBox buttonPanel = new HBox();

		Button createBtn = new Button("Create");
		createBtn.setOnAction(e -> {
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
				String name = result.get();
				ObservableList<PlaylistObject> selected = table.getSelectionModel().getSelectedItems();
				ArrayList<String> selectedNames = new ArrayList<String>();
				for (PlaylistObject po : selected) {
					selectedNames.add(po.getName());
				}
				String[] selectedNamesArray = selectedNames.toArray(new String[selectedNames.size()]);

				PlaylistWriter.createCustomPlaylist(name, selectedNamesArray);
			}
		});

		Button downloadBtn = new Button("Download");
		buttonPanel.getChildren().add(createBtn);
		buttonPanel.getChildren().add(downloadBtn);

		setTop(buttonPanel);
		setCenter(table);
	}

}
