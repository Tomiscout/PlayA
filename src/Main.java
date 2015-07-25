import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
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
			primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("Icon.png")));
			scene = new Scene(gui, 800, 530);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		KeyListener kl = new KeyListener();
	}
	
	@Override
	public void stop(){
		JIntellitype.getInstance().cleanUp();
	}
	

	public static Stage getPrimaryStage() {
		return pStage;
	}

}

// TODO

// downloading from yt
// drag'n'drop
// song ratios
