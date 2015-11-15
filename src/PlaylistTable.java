import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@SuppressWarnings("unchecked")
public class PlaylistTable extends TableView{
	public ObservableList<PlaylistObject> data = FXCollections.observableArrayList();

	public PlaylistTable() {
		setEditable(false);
		setMaxWidth(260);
		setMaxHeight(800);
		setPrefHeight(800);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<PlaylistObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setMaxWidth(224);
		nameColumn.setMinWidth(80);
		nameColumn.setPrefWidth(138);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<PlaylistObject, Integer> countColumn = new TableColumn("Songs");
		countColumn.setMaxWidth(40);
		countColumn.setMinWidth(26);
		countColumn.setCellValueFactory(new PropertyValueFactory<>("songs"));

		TableColumn<PlaylistObject, String> timeColumn = new TableColumn("Length");
		timeColumn.setMaxWidth(76);
		timeColumn.setMinWidth(40);
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
		
		// Double click listener
		setRowFactory(tv -> {
			TableRow<PlaylistObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					PlaylistObject rowData = row.getItem();
					PlaylistController.openPlaylist(rowData.getName(), true);
				}
			});
			return row;
		});
		
		getColumns().addAll(nameColumn, countColumn, timeColumn);
		setItems(data);
	}
	
	
}
