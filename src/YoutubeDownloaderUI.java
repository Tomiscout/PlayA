import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.stage.DirectoryChooser;

public class YoutubeDownloaderUI extends BorderPane {
	static ObservableList<ListItem> itemList = FXCollections.observableArrayList();
	private static int listIdCount = 0;
	
	ListView itemView;
	HBox buttonPane;
	static Label infoLabel = new Label();
	static Stage downloadOptionsWindow;

	public YoutubeDownloaderUI() {
		getStylesheets().add("DownloaderTheme.css");
		itemView = new ListView(itemList);
		itemView.setSelectionModel(new DisabledSelectionModel<ListItem>());

		itemView.setCellFactory(new Callback<ListView<ListItem>, ListCell<ListItem>>() {
			@Override
			public ListCell<ListItem> call(ListView<ListItem> param) {
				ListCell<ListItem> cell = new ListCell<ListItem>() {
					@Override
					public void updateItem(ListItem item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setMinHeight(48);

							GridPane cellContent = new GridPane();
							ColumnConstraints column1 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
							column1.setHgrow(Priority.ALWAYS);
							ColumnConstraints column2 = new ColumnConstraints(130);
							cellContent.getColumnConstraints().addAll(column1, column2); // first
																							// column
																							// gets
																							// any
																							// extra
																							// width
							cellContent.setMinHeight(32);
							cellContent.setAlignment(Pos.CENTER_LEFT);

							Label nameLabel = new Label(item.getName());
							nameLabel.setFont(new Font("Tahoma", 16));

							ProgressIndicatorBar bar = new ProgressIndicatorBar(item.getSongCount());
							bar.setMinWidth(64);
							bar.setMinHeight(cellContent.getHeight());
							bar.setWorkDone(item.getDownloadedSongCount());

							nameLabel.setMaxWidth(itemView.getWidth() - bar.getWidth());

							cellContent.add(nameLabel, 0, 0);
							cellContent.add(bar, 1, 0);
							setGraphic(cellContent);
						}
					}
				};

				return cell;
			}
		});

		buttonPane = new HBox();
		Button pasteBtn = new Button("Paste link");
		pasteBtn.setOnAction(e -> {
			// TODO add item to list
			String clipboard = null;
			try {
				clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println("Clipboard:" + clipboard);
			YtDownloadUtils.parseLink(clipboard);
		});

		buttonPane.getChildren().addAll(pasteBtn, infoLabel);

		setCenter(itemView);
		setBottom(buttonPane);
		
		//Starts main downloader thread
		new DownloadThreadManager();
	}

	public static void WriteInfo(String i) {
		infoLabel.setText(i);
	}

	private static int provideListItemId(){
		listIdCount++;
		return listIdCount;
	}
	
	static class ListItem {
		private int id;
		private String name;
		private int songCount;
		private int downloadedSongCount = 0;

		public ListItem(String name) {
			this.name = name;
			this.songCount = 1;
			id = provideListItemId();
		}

		public ListItem(String name, int songCount) {
			this.name = name;
			this.songCount = songCount;
			id = provideListItemId();
		}

		public int getDownloadedSongCount() {
			return downloadedSongCount;
		}

		public void setDownloadedSongCount(int downloadedSongCount) {
			this.downloadedSongCount = downloadedSongCount;
		}

		public String getName() {
			return name;
		}

		public int getSongCount() {
			return songCount;
		}
		
		public int getId(){
			return id;
		}
	}
	
	public static ListItem getListItem(int id){
		for(ListItem item : itemList){
			if(item.getId() == id){
				return item;
			}
		}
		return null;
	}

	public static void displayDownloadOptions(String videoId, String playlistId) {
		downloadOptionsWindow = new Stage();
		downloadOptionsWindow.initModality(Modality.APPLICATION_MODAL);
		downloadOptionsWindow.setTitle("Download options");
		downloadOptionsWindow.setWidth(410);
		downloadOptionsWindow.setResizable(false);

		if(playlistId == null)	downloadOptionsWindow.setHeight(114);
		else 					downloadOptionsWindow.setHeight(136);
		
		Scene scene = new Scene(new DownloadOptionsPane(videoId, playlistId));
		downloadOptionsWindow.setScene(scene);
		downloadOptionsWindow.showAndWait();
	}

	public static void closeDownloadOptions() {
		if (downloadOptionsWindow != null) {
			downloadOptionsWindow.close();
		}
	}

	static RadioButton rb1 = null;
	static CheckBox cb = null;
	static class DownloadOptionsPane extends GridPane {
		public DownloadOptionsPane(String videoId, String playlistId) {
			if (videoId != null) {
				TextField tf = new TextField(); // TODO add default dir
				Button chooserButton = new Button("Select folder");
				chooserButton.setMaxWidth(Double.MAX_VALUE);
				chooserButton.setOnAction(e -> {
					DirectoryChooser chooser = new DirectoryChooser();
					chooser.setTitle("JavaFX Projects");
					File defaultDirectory = new File("c:/");
					chooser.setInitialDirectory(defaultDirectory);
					File selectedDirectory = chooser.showDialog(downloadOptionsWindow);
					tf.setText(selectedDirectory.getAbsolutePath());
				});
				
				Button downloadButton = new Button("Download");
				downloadButton.setMaxWidth(Double.MAX_VALUE);
				downloadButton.setOnAction(e -> {
					System.out.println("*downloads*");
					
					File tempFile = new File(tf.getText());
					if(tempFile.exists() && tempFile.isDirectory()){
						if(rb1.isSelected()){
							if(cb != null && cb.isSelected()){
								//sync feature
								DownloadThreadManager.addToQueue(playlistId, tempFile, true);
							}else{
								//Add playlist to download quota
								DownloadThreadManager.addToQueue(playlistId, tempFile, false);
							}
						}else{
							//Add to download quota
							DownloadThreadManager.addToQueue(new DownloadThreadManager.YoutubeVideo("le neim", videoId), tempFile);
						}
						closeDownloadOptions();
					}else{
						System.out.println("Directory doesn't exist");
					}
				});
				
				if(playlistId != null){
					final ToggleGroup group = new ToggleGroup();

					rb1 = new RadioButton("Playlist");
					rb1.setToggleGroup(group);
					rb1.setSelected(true);

					RadioButton rb2 = new RadioButton("One video only");
					rb2.setToggleGroup(group);
					
					cb = new CheckBox("Synced");
					
					group.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
						if(group.getSelectedToggle() == rb1){
							cb.setDisable(false);
						}else{
							cb.setDisable(true);
						}
					});
					
					add(rb1,0,2);
					add(cb,1,2);
					add(rb2,0,3);
						
					WriteInfo("");
				}
				
				add(new Label("Download to:"), 0, 0);
				add(tf, 0, 1,2,1);
				add(chooserButton,2,1);
				add(downloadButton, 2,2);
				
				ColumnConstraints column1 = new ColumnConstraints();
			    column1.setPercentWidth(38);
			    ColumnConstraints column2 = new ColumnConstraints();
			    column2.setPercentWidth(38);
			    ColumnConstraints column3 = new ColumnConstraints();
			    column3.setPercentWidth(24);
			    getColumnConstraints().addAll(column1, column2, column3); 
				
			    setPadding(new Insets(0, 4, 4, 4));
			    setHgap(3);
			    setVgap(6);
				
				}else{
					WriteInfo("Bad link");
					closeDownloadOptions();
				}
			}
		}
	}

	class ProgressIndicatorBar extends StackPane {
		private int workDone = 0;
		private int totalWork;

		final private ProgressBar bar = new ProgressBar();
		final private Text text = new Text();

		final private static int DEFAULT_LABEL_PADDING = 5;

		ProgressIndicatorBar(int totalWork) {
			this.totalWork = totalWork;
			bar.setMaxWidth(Double.MAX_VALUE);
			bar.setProgress(0);

			getChildren().setAll(bar, text);
		}

		public double getWorkDone() {
			return workDone;
		}

		//How many items completed
		public void setWorkDone(int workDone) {
			this.workDone = workDone;
			bar.setProgress(workDone/totalWork);
			text.setText(workDone + "/" + totalWork);
		}
		
		public double getTotalWork() {
			return totalWork;
		}

		public void setTotalWork(int totalWork) {
			this.totalWork = totalWork;
		}
	}
	
	class DisabledSelectionModel<T> extends MultipleSelectionModel<T> {
		DisabledSelectionModel() {
			super.setSelectedIndex(-1);
			super.setSelectedItem(null);
		}

		@Override
		public ObservableList<Integer> getSelectedIndices() {
			return FXCollections.<Integer> emptyObservableList();
		}

		@Override
		public ObservableList<T> getSelectedItems() {
			return FXCollections.<T> emptyObservableList();
		}

		@Override
		public void selectAll() {
		}

		@Override
		public void selectFirst() {
		}

		@Override
		public void selectIndices(int index, int... indicies) {
		}

		@Override
		public void selectLast() {
		}

		@Override
		public void clearAndSelect(int index) {
		}

		@Override
		public void clearSelection() {
		}

		@Override
		public void clearSelection(int index) {
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public boolean isSelected(int index) {
			return false;
		}

		@Override
		public void select(int index) {
		}

		@Override
		public void select(T item) {
		}

		@Override
		public void selectNext() {
		}

		@Override
		public void selectPrevious() {
		}
	
	}
	
