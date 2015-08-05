import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SongTable extends TableView {
	
	public static ObservableList<SongObject> data = FXCollections.observableArrayList();
	
	public SongTable() {
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setPrefHeight(4068);
		setPrefWidth(4068);
		setEditable(false);
		setItems(data);

		// TableColumns
		TableColumn<SongObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setPrefWidth(380);
		nameColumn.setMinWidth(160);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<SongObject, String> lengthColumn = new TableColumn("Length");
		lengthColumn.setMaxWidth(69);
		lengthColumn.setMinWidth(48);
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

		getColumns().addAll(nameColumn, lengthColumn);

		// Double click listener
		setRowFactory(tv -> {
			TableRow<SongObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					SongObject rowData = row.getItem();
					PlaylistController.playSongFilename(PlaylistController.getSongFilepath(rowData.getName()));
				}
			});
			return row;
		});
	}
}
