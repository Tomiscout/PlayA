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
	static File libDir = new File(System.getenv("APPDATA") + "\\Tomiscout\\PlayA\\lib\\");
	static boolean is64bit = false;

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
		initializeLibraries();

		@SuppressWarnings("unused")
		KeyListener kl = new KeyListener();
	}

	@Override
	public void stop() {
		JIntellitype.getInstance().cleanUp();
		DownloadThreadManager.stopThreads();
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}

	private void initializeLibraries() {
		if(!libDir.exists()) libDir.mkdirs();
		
		if (System.getProperty("os.name").contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}

		try {
			// Loads correct version of this dll
			String JintelliLib;
			if (is64bit) {
				JintelliLib = libDir.getAbsolutePath()+"\\JIntellitype64.dll";
			} else {
				JintelliLib = libDir.getAbsolutePath()+"\\JIntellitype.dll";
			}
			JIntellitype.setLibraryLocation(JintelliLib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	}


// TODO

// downloading from yt
// drag'n'drop
// song ratios
