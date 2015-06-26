import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

	static Stage pStage;
	Button button;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		pStage = primaryStage;
		MainGui gui = new MainGui();

		primaryStage.setTitle("PlayA");
		Scene scene = new Scene(gui, 800, 520);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public static Stage getPrimaryStage() {
		return pStage;
	}
}

//TODO

//song table,buttons,seekbar
//more efficient playlist file,  probably. m3u or simmilar
//next, previous songs
//shuffle

//custom playlist
//Work on css
//downloading from yt
//drag'n'drop

//layouts http://docs.oracle.com/javafx/2/layout/builtin_layouts.htm