package main.tomiscout.ui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import main.tomiscout.playlistControl.PlaylistController;
import main.tomiscout.playlistControl.PlaylistWriter;

public class SongTable extends TableView<PlaylistWriter.SongObject> {
	
	public ObservableList<PlaylistWriter.SongObject> data = FXCollections.observableArrayList();
	
	public SongTable() {
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setPrefHeight(4068);
		setPrefWidth(4068);
		setEditable(false);
		setItems(data);

		// TableColumns
		TableColumn<PlaylistWriter.SongObject, String> nameColumn = new TableColumn<PlaylistWriter.SongObject, String>("Name");
		nameColumn.setPrefWidth(380);
		nameColumn.setMinWidth(160);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<PlaylistWriter.SongObject, String> lengthColumn = new TableColumn<PlaylistWriter.SongObject, String>("Length");
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
					PlaylistController.playSong(rowData.getFile(),true);
				}
			});
			return row;
		});
	}
	
	public ObservableList<PlaylistWriter.SongObject> getData(){
		return data;
	}
	public void setList(ObservableList<PlaylistWriter.SongObject> list){
		setItems(list);
	}
	public void resetList(){
		setItems(data);
	}
}
