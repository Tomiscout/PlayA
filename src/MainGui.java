import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class MainGui extends BorderPane{

	public MainGui(){
		//Adds top Player pane
		HBox topPane = new HBox();
		setTop(topPane);
		
		// Adds center pane
		BorderPane centerPane = new BorderPane();
		setCenter(centerPane);
		
		//Adds song pane
		StackPane songPane = new StackPane();
		centerPane.setCenter(songPane);
		
		//Ads right pane
		VBox rightPane = new VBox();
		centerPane.setRight(rightPane);
		
		PlaylistPane playlistPane = new PlaylistPane();
		ScrollPane controlPane = new ScrollPane();
		
		rightPane.getChildren().addAll(playlistPane,controlPane);
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
}
