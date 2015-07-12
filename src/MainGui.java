import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainGui extends HBox {

	TableView<SongObject> table;
	static Slider seekBar;
	public static ObservableList<SongObject> data = FXCollections.observableArrayList();
	static Label songLabel = null;
	private static boolean isSeeking = false;
	private boolean isShrinked = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MainGui() {
		
		getStylesheets().add("MainTheme.css");
		// TableView
		table = new TableView();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(4068);
		table.setPrefWidth(4068);
		table.setEditable(false);
		table.setItems(data);

		// TableColumns
		TableColumn<SongObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setPrefWidth(380);
		nameColumn.setMinWidth(160);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<SongObject, String> lengthColumn = new TableColumn("Length");
		lengthColumn.setMaxWidth(69);
		lengthColumn.setMinWidth(48);
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
		VBox centerPane = new VBox();
		centerPane.setMaxHeight(Double.MAX_VALUE);
		centerPane.setSpacing(4);

		// Ads right pane
		VBox rightPane = new VBox();

		VBox topPane = new VBox();

		GridPane optionPane = new GridPane();

		// Playing options
		ObservableList<String> options = FXCollections.observableArrayList("Normal", "Shuffle");
		final ComboBox comboBox = new ComboBox(options);
		comboBox.setValue("Shuffle");
		comboBox.valueProperty().addListener((ov, s, s1) -> {
			PlaylistController.setPlayingMode((String) s1);
		});
		optionPane.add(new Text("Playing mode "), 0, 0);
		optionPane.add(comboBox, 1, 0);

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
		seekBar.setPadding(new Insets(5,0,4,0));
		
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
		volumeBar.setPrefWidth(100);
		volumeBar.setPadding(new Insets(5,0,4,0));
		volumeBar.valueProperty().addListener(e -> {
			FXMediaPlayer.setVolume(volumeBar.getValue());
		});

		// Play
		Button playBtn = new Button(">");
		playBtn.setOnAction(e -> {
			if(PlaylistController.currentSong == null){
			SongObject selected = table.getSelectionModel().getSelectedItem();

			if (selected == null)
				return;

			PlaylistController.playSongFilename(PlaylistController.getSongFilepath(selected.getName()));
			}else{
				PlayerController.play(PlaylistController.currentSong);
			}
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
		Button nextBtn = new Button(">>");
		nextBtn.setOnAction(e -> {
			PlaylistController.playNextSong();
		});

		// Previous
		Button previousBtn = new Button("<<");
		previousBtn.setOnAction(e -> {
			PlaylistController.playPreviousSong();
		});

		//
		Button shrinkBtn = new Button("卍");
		shrinkBtn.setOnAction(e -> {
			rightPane.setManaged(isShrinked);
			rightPane.setVisible(isShrinked);

			if (isShrinked) {
				shrinkBtn.setText("卍");
				Main.pStage.setWidth(Main.pStage.getWidth() + rightPane.getWidth());
			} else {
				shrinkBtn.setText("☭");
				Main.pStage.setWidth(Main.pStage.getWidth() - rightPane.getWidth());
			}

			isShrinked = !isShrinked;
		});


		playerPane.getChildren().addAll(playBtn, pauseBtn, stopBtn, nextBtn, previousBtn, seekBar, volumeBar,
				shrinkBtn);

		topPane.getChildren().addAll(songLabel, playerPane);

		centerPane.getChildren().addAll(topPane, table);

		PlaylistPane playlistPane = new PlaylistPane();

		optionPane.setPadding(new Insets(4,0,0,10));
		rightPane.getChildren().addAll(playlistPane, optionPane);

		getChildren().addAll(centerPane, rightPane);
	}

	public static void setSongName(String s) {
		songLabel.setText(s);
	}

	public static void setSeekValue(double d) {
		if (!isSeeking)
			seekBar.setValue(d);
	}
}
