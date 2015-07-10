import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

	static Stage pStage;
	static Scene scene;
	Button button;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			pStage = primaryStage;
			MainGui gui = new MainGui();

			primaryStage.setTitle("PlayA");
			scene = new Scene(gui, 800, 520);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}
}

// TODO

// music visual canvas
// downloading from yt
// drag'n'drop
// song ratios

// layouts http://docs.oracle.com/javafx/2/layout/builtin_layouts.htm