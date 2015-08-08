import java.io.File;
import java.net.URISyntaxException;

import com.melloware.jintellitype.JIntellitype;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
			primaryStage.getIcons().add(FileUtils.getAssetsImage("Icon.png"));
			scene = new Scene(gui, 800, 530);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// initializes libraries
		LibraryLoader.initializeLibraries();

		// puts itself in the workdir folder
		MakeLauncher();

		@SuppressWarnings("unused")
		KeyListener kl = new KeyListener();
	}

	@Override
	public void stop() {
		JIntellitype.getInstance().cleanUp();
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}

	private static void MakeLauncher() {
		File launcherFile = null;
		try {
			launcherFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if(launcherFile.getAbsolutePath().endsWith("bin")) return; //if this program is not opened from .jar (but eclipse)
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File newLauncher = new File(FileUtils.getWorkDirectory() + "\\"+launcherFile.getName());
		FileUtils.copyFile(launcherFile, newLauncher);
	}
}

// TODO

// downloading from yt
// drag'n'drop
// song ratios
