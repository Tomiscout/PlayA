import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SongTable extends TableView {
	
	private static ObservableList<PlaylistWriter.SongObject> data = FXCollections.observableArrayList();
	
	public SongTable() {
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setPrefHeight(4068);
		setPrefWidth(4068);
		setEditable(false);
		setItems(data);

		// TableColumns
		TableColumn<PlaylistWriter.SongObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setPrefWidth(380);
		nameColumn.setMinWidth(160);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<PlaylistWriter.SongObject, String> lengthColumn = new TableColumn("Length");
		lengthColumn.setMaxWidth(69);
		lengthColumn.setMinWidth(48);
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("lengthString"));

		getColumns().addAll(nameColumn, lengthColumn);

		// Double click listener
		setRowFactory(tv -> {
			TableRow<PlaylistWriter.SongObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					PlaylistWriter.SongObject rowData = row.getItem();
					PlaylistController.playSong(rowData.getFile());
				}
			});
			return row;
		});
	}
	
	public static ObservableList<PlaylistWriter.SongObject> getData(){
		return data;
	}
}
