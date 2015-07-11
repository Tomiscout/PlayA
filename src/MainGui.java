import java.util.ArrayList;

import javax.swing.event.ChangeListener;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainGui extends HBox {

	TableView<SongObject> table;
	static Slider seekBar;
	public static ObservableList<SongObject> data = FXCollections.observableArrayList();
	static Label songLabel = null;
	private static boolean isSeeking = false;
	private boolean isShrinked = false;
	
	static BorderPane spectrumPane;
	StackPane glassTableView;
	private static Group spectrum;
	private static int cones = 128;
	private static int gap = 1;
	private static double coneWidth;
	private static ArrayList<Line> r = new ArrayList<Line>();

	/**
	 * 
	 */
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
		comboBox.setValue("Normal");
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


		// Adds spectrum canvas	
		spectrum = new Group();
		coneWidth = 2;
		
		spectrumPane = new BorderPane();
		spectrumPane.setPrefHeight(64);
		spectrumPane.setMinHeight(64);
		spectrumPane.setBottom(spectrum);
		
		glassTableView = new StackPane();
		glassTableView.getStylesheets().add("TransparentTableView.css");
		Label tla = new Label("Sdsfdsfsdfsdf");
	    StackPane.setAlignment(table, Pos.BOTTOM_CENTER);
	    glassTableView.getChildren().addAll(table);

		StackPane stackedCenterPane = new StackPane();
		stackedCenterPane.getChildren().add(spectrumPane);
		stackedCenterPane.getChildren().add(glassTableView);
		
		playerPane.getChildren().addAll(playBtn, pauseBtn, stopBtn, nextBtn, previousBtn, seekBar, volumeBar,
				shrinkBtn);

		topPane.getChildren().addAll(songLabel, playerPane);

		centerPane.getChildren().addAll(topPane, stackedCenterPane);

		for (int i = 0; i < 128; i++) {
			Line l = new Line();
			l.setStartY(spectrumPane.getHeight());
			l.setEndY(0);
			l.setStartX(i*coneWidth+i*gap);
			l.setEndX(i*coneWidth+i*gap);
			r.add(l);
		}
		spectrum.getChildren().addAll(r);
		
		PlaylistPane playlistPane = new PlaylistPane();
		ScrollPane controlPane = new ScrollPane();

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
	
	public static void updateMagnitudes(float[] mag){
		
		for (int i = 0; i < mag.length; i++) {
			r.get(i).setEndY(-mag[i]-spectrumPane.getHeight());
			r.get(i).setStartY(36);
		}
	}
}
