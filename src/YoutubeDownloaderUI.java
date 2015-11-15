import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import com.jfoenix.controls.JFXProgressBar;

import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class YoutubeDownloaderUI extends BorderPane {
	static final int MAXTHREADS = 16;
	static ArrayList<ListItem> itemList = new ArrayList<ListItem>();
	static ObservableList<ListItem> visibleList = FXCollections.observableArrayList();
	static ObservableList<SearchListItem> searchList = FXCollections.observableArrayList();

	static ListView<ListItem> itemView;
	static ListView<SearchListItem> searchView;
	

	HBox buttonPane;
	static Label infoLabel = new Label();
	static Stage downloadOptionsWindow;
	static Stage channelSearchWindow;
	static ProgressBar playlistProgressBar;

	public YoutubeDownloaderUI() {
		// getStylesheets().add("DownloaderTheme.css");
		itemView = new ListView<ListItem>(visibleList);
		itemView.setSelectionModel(new DisabledSelectionModel<ListItem>());
		itemView.setMaxHeight(Double.MAX_VALUE);
		itemView.setCellFactory(new Callback<ListView<ListItem>, ListCell<ListItem>>() {
			@Override
			public ListCell<ListItem> call(ListView<ListItem> param) {
				ListCell<ListItem> cell = new ListCell<ListItem>() {
					@Override
					protected void updateItem(ListItem item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							if (empty || item == null) {
								setGraphic(null);
							} else {
								GridPane cellContent = new GridPane();
								ColumnConstraints column1 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
								column1.setHgrow(Priority.ALWAYS);
								ColumnConstraints column2 = new ColumnConstraints(130);
								cellContent.getColumnConstraints().addAll(column1, column2);

								cellContent.setAlignment(Pos.CENTER_LEFT);

								Label nameLabel = new Label(item.getName());
								nameLabel.setFont(new Font("Tahoma", 16));

								ProgressIndicatorBar bar = item.getBar();
								bar.setMinWidth(64);
								bar.setMinHeight(cellContent.getHeight());

								nameLabel.setMaxWidth(itemView.getWidth() - bar.getWidth());

								cellContent.add(nameLabel, 0, 0);
								cellContent.add(bar, 1, 0);
								setGraphic(cellContent);
							}

						}
					}
				};

				return cell;
			}
		});

		buttonPane = new HBox();
		buttonPane.setPadding(new Insets(4));
		buttonPane.setSpacing(4);
		Button pasteBtn = new Button("Paste link");
		pasteBtn.setOnAction(e -> {
			String clipboard = null;
			try {
				clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println("Clipboard:" + clipboard);
			YtDownloadUtils.parseLink(clipboard);
		});

		Button channelBtn = new Button("Search for channels");
		channelBtn.setOnAction(e -> {
			displaySearchWindow();
		});
		
		playlistProgressBar = new ProgressBar();
		playlistProgressBar.setPrefWidth(230);
		playlistProgressBar.setVisible(false);
		
		ObservableList<Integer> threadOptions = FXCollections.observableArrayList();
		for(int i = 1; i <= MAXTHREADS; i++) threadOptions.add(new Integer(i));
		
		ComboBox<Integer> threadCombo = new ComboBox<Integer>(threadOptions);
		threadCombo.setValue(new Integer(2));
		threadCombo.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override public void changed(ObservableValue<? extends Integer> ov, Integer t, Integer t1) {
            	DownloadThreadManager.setThreads(t1.intValue());
            }    
        });
		
		buttonPane.getChildren().addAll(pasteBtn, channelBtn, infoLabel, playlistProgressBar, threadCombo);

		setCenter(itemView);
		setBottom(buttonPane);

		setPadding(new Insets(4));
		
		// Starts main downloader thread
		new DownloadThreadManager();
	}
	
	public static void setPlaylistProgressVisible(boolean visible){
		Platform.runLater(new Runnable(){
			public void run(){
				playlistProgressBar.setVisible(visible);
			}
		});
	}
	
	public static void setPlaylistProgress(double progress){
		Platform.runLater(new Runnable(){
			public void run(){
				playlistProgressBar.setProgress(progress);
			}
		});
	}

	public static void WriteInfo(String i) {
		infoLabel.setText(i);
	}

	public static synchronized ListItem getListItem(int id) {
		for (ListItem item : itemList) {
			if (item.getId() == id)
				return item;
		}
		return null;
	}

	static class ListItem {
		private int id;
		private String name;
		private int songCount;
		private int downloadedSongCount = 0;
		ProgressIndicatorBar bar;

		// if int parent == -1, it means that this item is parent, else it
		// specifies parent item id
		public ListItem(String name, int id) {
			this.name = name;
			this.songCount = 1;
			this.id = id;
			this.bar = new ProgressIndicatorBar();
		}

		public int getDownloadedSongCount() {
			return downloadedSongCount;
		}

		public ProgressIndicatorBar getBar() {
			return bar;
		}

		public void plusDownloaded() {
			downloadedSongCount++;
			bar.setWorkDone(downloadedSongCount);
		}

		public String getName() {
			return name;
		}

		public int getSongCount() {
			return songCount;
		}

		public int getId() {
			return id;
		}

		public void setCompleted() {
			bar.setDisable(true);
			bar.setManaged(false);
			bar.setText("Done!");
		}
	}

	public static synchronized void addListItem(String name, int id) {
		itemList.add(new ListItem(name, id));
	}

	// Recalculates visible list items
	public static void refreshList() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				visibleList.clear();
				for (ListItem item : itemList) {
					visibleList.add(item);
				}
			}
		});
	}

	public static void refreshSearchList() {
		if (searchView != null) {
			searchView.setItems(null);
			searchView.setItems(searchList);
		}
	}

	public static void displayDownloadOptions(String videoId, String playlistId) {
		closeSearchWindow();
		Platform.runLater(new Runnable(){
			public void run() {
				downloadOptionsWindow = new Stage();
				downloadOptionsWindow.initModality(Modality.APPLICATION_MODAL);
				downloadOptionsWindow.setTitle("Download options");
				downloadOptionsWindow.setWidth(410);
				downloadOptionsWindow.setResizable(false);

				if (playlistId == null)
					downloadOptionsWindow.setHeight(114);
				else
					downloadOptionsWindow.setHeight(136);

				Scene scene = new Scene(new DownloadOptionsPane(videoId, playlistId));
				downloadOptionsWindow.setScene(scene);
				downloadOptionsWindow.showAndWait();
			}
		});
	}

	public static void closeDownloadOptions() {
		if (downloadOptionsWindow != null) {
			downloadOptionsWindow.close();
			downloadOptionsWindow = null;
		}
	}
	
	public static void displaySearchWindow(){
		closeDownloadOptions();
		channelSearchWindow = new Stage();
		channelSearchWindow.initModality(Modality.APPLICATION_MODAL);
		channelSearchWindow.setTitle("Channel search");
		channelSearchWindow.setWidth(360);
		channelSearchWindow.setHeight(472);

		Scene scene = new Scene(new ChannelSearchPane());
		channelSearchWindow.setScene(scene);
		channelSearchWindow.showAndWait();
	}
	public static void closeSearchWindow(){
		if(channelSearchWindow != null){
			channelSearchWindow.close();
			channelSearchWindow = null;
		}
	}

	static RadioButton rb1 = null;
	static CheckBox cb = null;

	static class ChannelSearchPane extends BorderPane {
		public ChannelSearchPane() {
			searchView = new ListView<SearchListItem>(searchList);
			searchView.setCellFactory(new Callback<ListView<SearchListItem>, ListCell<SearchListItem>>() {
				@Override
				public ListCell<SearchListItem> call(ListView<SearchListItem> param) {
					ListCell<SearchListItem> cell = new ListCell<SearchListItem>() {
						@Override
						protected void updateItem(SearchListItem item, boolean empty) {
							super.updateItem(item, empty);
							if (item != null) {
								if (empty || item == null) {
									setGraphic(null);
								} else {
									HBox cellContent = new HBox();

									ImageView image = new ImageView();
									image.setImage(item.getThumbnail());
									image.setFitHeight(64);
									image.setFitWidth(64);

									VBox textContent = new VBox();

									Label nameLabel = new Label(item.getName());
									nameLabel.setFont(new Font("Tahoma", 22));
									Label bottomLabel = new Label(item.getBottomText());
									bottomLabel.setFont(new Font("Tahoma", 14));

									textContent.getChildren().addAll(nameLabel, bottomLabel);

									cellContent.setSpacing(8);
									cellContent.getChildren().addAll(image, textContent);
									setGraphic(cellContent);
								}

							}
						}
					};

					return cell;
				}
			});

			searchView.setOnMouseClicked(click -> {
				if (click.getClickCount() == 2) {
					// Parses channel to download options window
					SearchListItem selected = searchView.getSelectionModel().getSelectedItem();
					if(selected != null){
						System.out.println("parsing :"+selected.getUploadId());
						displayDownloadOptions(null, selected.getUploadId());
					}
				}

			});

			HBox inputPane = new HBox();
			inputPane.setPadding(new Insets(4));
			inputPane.setSpacing(4);

			Label textLabel = new Label("Channel:");
			TextField inputField = new TextField();
			inputField.setOnKeyPressed(key -> {
				if (key.getCode().equals(KeyCode.ENTER)) {
					searchList.clear();
					YtDownloadUtils.SearchForChannel(inputField.getText());
				}
			});

			Button searchBtn = new Button("Search");
			searchBtn.setOnAction(e -> {
				searchList.clear();
				YtDownloadUtils.SearchForChannel(inputField.getText());
			});

			inputPane.getChildren().addAll(textLabel, inputField, searchBtn);

			setCenter(searchView);
			setTop(inputPane);
			setPadding(new Insets(4));
		}
	}

	public static void addSearchItem(String name, int videoCount, String uploadId, URL thumbnail) {
		searchList.add(new SearchListItem(name, videoCount, uploadId, thumbnail));
	}

	static class SearchListItem {
		protected int id;
		private String name;
		private String uploadId;
		private String bottomText = "";
		private Image thumbImage;

		public SearchListItem(String name, int videoCount, String uploadId, URL thumbnail) {
			this.name = name;
			this.uploadId = uploadId;
			bottomText = "Uploads: " + videoCount;

			if (thumbnail != null) {
				// Downloads image in separate thread
				new Thread() {
					public void run() {
						BufferedImage thumb = DataUtils.downloadImage(thumbnail);
						if(thumb != null) thumbImage = SwingFXUtils.toFXImage(thumb, null);
					}
				}.start();
			}
		}

		public String getBottomText() {
			return bottomText;
		}

		public String getName() {
			return name;
		}

		public String getUploadId() {
			return uploadId;
		}

		public Image getThumbnail() {
			return thumbImage;
		}
	}

	static class DownloadOptionsPane extends GridPane {
		public DownloadOptionsPane(String videoId, String playlistId) {
			if (videoId != null || playlistId != null) {
				TextField tf = new TextField(); // TODO add default dir
				Button chooserButton = new Button("Select folder");
				chooserButton.setMaxWidth(Double.MAX_VALUE);
				chooserButton.setOnAction(e -> {
					DirectoryChooser chooser = new DirectoryChooser();
					chooser.setTitle("Choose folder");
					File defaultDirectory = new File("c:/");
					chooser.setInitialDirectory(defaultDirectory);
					File selectedDirectory = chooser.showDialog(downloadOptionsWindow);
					if(selectedDirectory != null) tf.setText(selectedDirectory.getAbsolutePath());
				});

				Button downloadButton = new Button("Download");
				downloadButton.setMaxWidth(Double.MAX_VALUE);
				downloadButton.setOnAction(e -> {
					File tempFile = new File(tf.getText());
					if (tempFile.exists() && tempFile.isDirectory()) {
						if (rb1 != null && rb1.isSelected()) {
							if (cb != null && cb.isSelected() && playlistId != null) {
								// merge feature
								DownloadThreadManager.addToQueue(playlistId, tempFile, true);
							} else {
								// Add playlist to download quota
								DownloadThreadManager.addToQueue(playlistId, tempFile, false);
							}
						} else {
							// Add to download quota
							if(videoId != null)	DownloadThreadManager.addToQueueSingle(videoId, tempFile);
						}
						closeDownloadOptions();
					} else {
						System.out.println("Directory doesn't exist");
					}
				});

				if (playlistId != null) {
					final ToggleGroup group = new ToggleGroup();

					rb1 = new RadioButton("Playlist");
					rb1.setToggleGroup(group);
					rb1.setSelected(true);

					RadioButton rb2 = new RadioButton("One video only");
					rb2.setToggleGroup(group);

					cb = new CheckBox("Merge");
					cb.setSelected(true);
					
					group.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
						if (group.getSelectedToggle() == rb1) {
							cb.setDisable(false);
						} else {
							cb.setDisable(true);
						}
					});

					add(rb1, 0, 2);
					add(cb, 1, 2);
					add(rb2, 0, 3);

					WriteInfo("");
				}

				add(new Label("Download to:"), 0, 0);
				add(tf, 0, 1, 2, 1);
				add(chooserButton, 2, 1);
				add(downloadButton, 2, 2);

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

			} else {
				WriteInfo("Bad link");
				closeDownloadOptions();
			}
		}
	}

	static class ProgressIndicatorBar extends StackPane {
		private double workDone;
		private double totalWork = -1;

		final private ProgressBar bar = new ProgressBar();
		final private Text text = new Text();

		ProgressIndicatorBar() {
			bar.setMaxWidth(Double.MAX_VALUE);
			setWorkDone(0);

			getChildren().setAll(bar, text);
		}

		public double getWorkDone() {
			return workDone;
		}

		// How many items completed
		public void setWorkDone(double workDone) {
			if (totalWork != -1) {
				this.workDone = workDone;
				setProgress(workDone / totalWork);
				setText((int) workDone + "/" + (int) totalWork);
			} else {
				setProgress(-1);
				setText("Please wait...");
			}
		}

		public void setText(String barText) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					text.setText(barText);
				}
			});
		}

		public double getTotalWork() {
			return totalWork;
		}

		public void setTotalWork(int totalWork) {
			this.totalWork = totalWork;
		}

		public void setProgress(double progress) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					bar.setProgress(progress);
				}
			});
		}

		public void complete() {
			setProgress(1);
			setText("Done!");
		}
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
