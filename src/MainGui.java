import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainGui extends BorderPane {

	TableView<SongObject> table;
	Slider slider;
	public static ObservableList<SongObject> data = FXCollections
			.observableArrayList();
	static Label songLabel = null;

	public MainGui() {
		
		
		// TableView
		table = new TableView();
		table.setEditable(false);
		table.setItems(data);

		// TableColumns
		TableColumn<SongObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setPrefWidth(459);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<SongObject, String> lengthColumn = new TableColumn("Length");
		lengthColumn.setMaxWidth(70);
		lengthColumn.setPrefWidth(70);
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

		table.getColumns().addAll(nameColumn, lengthColumn);

		// Double click listener
		table.setRowFactory(tv -> {
			TableRow<SongObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					SongObject rowData = row.getItem();
					PlaylistController.playSongFilename(PlaylistController
							.getSongFilepath(rowData.getName()));
					setSongName(rowData.getName());
				}
			});
			return row;
		});

		VBox topPane = new VBox();

		songLabel = new Label("Sng name");
		songLabel.setFont(new Font("Impact", 24));

		// Adds top Player pane
		HBox playerPane = new HBox();

		// Slider
		slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(40);

		// Play
		Button playBtn = new Button(">");
		playBtn.setOnAction(e -> {
			SongObject selected = table.getSelectionModel().getSelectedItem();

			PlaylistController.playSongFilename(PlaylistController
					.getSongFilepath(selected.getName()));
			setSongName(selected.getName());
		});

		// Pause
		Button pauseBtn = new Button("❙❙");
		pauseBtn.setOnAction(e -> {
			PlayerController.pause();
		});

		// Stop
		Button stopBtn = new Button("■");
		stopBtn.setOnAction(e -> {
			PlayerController.stop();
		});

		// Next
		Button nextBtn = new Button(">|");
		nextBtn.setOnAction(e -> {
			PlaylistController.playNextSong();
		});

		// Previous
		Button previousBtn = new Button("|<");
		previousBtn.setOnAction(e -> {
			PlaylistController.playPreviousSong();
		});

		playerPane.getChildren().addAll(slider, playBtn, pauseBtn, stopBtn,
				nextBtn, previousBtn);

		topPane.getChildren().addAll(songLabel, playerPane);
		setTop(topPane);

		// Adds center pane
		BorderPane centerPane = new BorderPane();
		setCenter(centerPane);

		// Adds song pane
		StackPane songPane = new StackPane();
		songPane.getChildren().add(table);
		centerPane.setCenter(songPane);

		// Ads right pane
		VBox rightPane = new VBox();
		centerPane.setRight(rightPane);

		PlaylistPane playlistPane = new PlaylistPane();
		ScrollPane controlPane = new ScrollPane();

		rightPane.getChildren().addAll(playlistPane, controlPane);

	}
	
	public static void setSongName(String s){
		songLabel.setText(s);
	}
}
