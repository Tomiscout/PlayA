import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class MainGui extends BorderPane{

	TableView table;
	Slider slider;
	public static ObservableList<SongObject> data = FXCollections.observableArrayList();
	
	
	public MainGui(){
		
		//TableView
		table = new TableView();
		table.setEditable(false);
		table.setItems(data);

		//TableColumns
		TableColumn<SongObject, String> nameColumn = new TableColumn("Name");
		nameColumn.setPrefWidth(459);
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		TableColumn<SongObject, String> lengthColumn = new TableColumn("Length");
		lengthColumn.setMaxWidth(70);
		lengthColumn.setPrefWidth(70);
		lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
		
		table.getColumns().addAll(nameColumn, lengthColumn);
		
		//Slider
		slider = new Slider();
		slider.setMin(0);
		slider.setMax(100);
		slider.setValue(40);
		
		//Adds top Player pane
		HBox topPane = new HBox();
		topPane.getChildren().add(slider);
		setTop(topPane);
		
		// Adds center pane
		BorderPane centerPane = new BorderPane();
		setCenter(centerPane);
		
		//Adds song pane
		StackPane songPane = new StackPane();
		songPane.getChildren().add(table);
		centerPane.setCenter(songPane);
		
		//Ads right pane
		VBox rightPane = new VBox();
		centerPane.setRight(rightPane);
		
		PlaylistPane playlistPane = new PlaylistPane();
		ScrollPane controlPane = new ScrollPane();
		
		rightPane.getChildren().addAll(playlistPane,controlPane);
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
}
