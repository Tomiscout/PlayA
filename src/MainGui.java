import javax.swing.event.ChangeListener;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
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
	static Slider seekBar;
	public static ObservableList<SongObject> data = FXCollections.observableArrayList();
	static Label songLabel = null;
	private static boolean isSeeking = false;
	private boolean isShrinked = false;

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
		lengthColumn.setMaxWidth(69);
		lengthColumn.setPrefWidth(69);
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

		table.getColumns().addAll(nameColumn, lengthColumn);

		// Double click listener
		table.setRowFactory(tv -> {
			TableRow<SongObject> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					SongObject rowData = row.getItem();
					PlaylistController.playSongFilename(PlaylistController.getSongFilepath(rowData.getName()));
					setSongName(rowData.getName());
				}
			});
			return row;
		});

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

		VBox topPane = new VBox();

		songLabel = new Label("Sng name");
		songLabel.setFont(new Font("Impact", 18));

		// Adds top Player pane
		HBox playerPane = new HBox();
		playerPane.setSpacing(2);

		// Slider
		seekBar = new Slider();
		seekBar.setPrefWidth(256);
		seekBar.setMin(0);
		seekBar.setMax(100);
		seekBar.setValue(1.0);

		seekBar.setOnMousePressed(e -> {
			isSeeking = true;
		});
		seekBar.setOnMouseClicked(m -> {
			FXMediaPlayer.seek(seekBar.getValue());
			isSeeking = false;
		});
		seekBar.setOnMouseReleased(e -> isSeeking = false);

		Slider volumeBar = new Slider();
		volumeBar.setMax(1);
		volumeBar.setValue(1);
		volumeBar.setPrefWidth(90);
		volumeBar.valueProperty().addListener(e -> {
			FXMediaPlayer.setVolume(volumeBar.getValue());
		});

		// Play
		Button playBtn = new Button(">");
		playBtn.setOnAction(e -> {
			SongObject selected = table.getSelectionModel().getSelectedItem();

			if (selected == null)
				return;

			PlaylistController.playSongFilename(PlaylistController.getSongFilepath(selected.getName()));
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

		//
		Button shrinkBtn = new Button("卍");
		shrinkBtn.setFont(new Font("Arial", 16));
		shrinkBtn.setOnAction(e -> {
			rightPane.setManaged(isShrinked);
			rightPane.setVisible(isShrinked);

			if(isShrinked){
				shrinkBtn.setText("卍");
				Main.pStage.setWidth(Main.pStage.getWidth()+rightPane.getWidth());
			}else{
				shrinkBtn.setText("☭");
				Main.pStage.setWidth(Main.pStage.getWidth()-rightPane.getWidth());
			}
			
			isShrinked = !isShrinked;
		});

		playerPane.getChildren().addAll(playBtn, pauseBtn, stopBtn, nextBtn, previousBtn, seekBar, volumeBar,
				shrinkBtn);

		topPane.getChildren().addAll(songLabel, playerPane);
		setTop(topPane);

		PlaylistPane playlistPane = new PlaylistPane();
		ScrollPane controlPane = new ScrollPane();

		rightPane.getChildren().addAll(playlistPane, controlPane);

	}

	public static void setSongName(String s) {
		songLabel.setText(s);
	}

	public static void setSeekValue(double d) {
		if (!isSeeking)
			seekBar.setValue(d);
	}
}
