import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.melloware.jintellitype.JIntellitype;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Main extends Application {
	static String appdata = System.getenv("APPDATA");
	static File libDir = new File(appdata + "\\Tomiscout\\PlayA\\lib\\");
	static File mainJar = new File(appdata + "\\Tomiscout\\PlayA\\PlayA.jar");

	static Stage pStage;
	static Scene scene;
	Button button;

	static boolean is64bit = false;

	public static void main(String[] args) {
		if(Main.class.getResource("Main.class").toString().startsWith("jar:")){
			//If started from jar
			if (checkIfGoodDirectory()) {
				System.out.println("Launching jar");
				launch(args);
			} else {
				// Launch app from appdata folder
				if (mainJar.exists()) {
					System.out.println("Launching from appdata...");
					launchJar();
				} else
					System.out.println("Reinstall application!");
				System.exit(1);
			}
		}else{
			//if started from eclipse
			launch(args);
		}
		
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			pStage = primaryStage;
			MainGui gui = new MainGui();

			primaryStage.setTitle("PlayA "+"0.7");
			primaryStage.getIcons().add(FileUtils.getAssetsImage("Icon.png"));
			scene = new Scene(gui, 800, 560);
			/*scene.widthProperty().addListener(new ChangeListener<Number>() {
			    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
			        System.out.println("Width: " + newSceneWidth);
			    }
			});*/
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
		System.exit(1);
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}
	
	public static void launchJar(){
		try {
			Runtime r = Runtime.getRuntime();
			r.exec("java -jar \"" + mainJar.getAbsolutePath() + "\"");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void initializeLibraries() {
		if (!libDir.exists())
			libDir.mkdirs();

		if (System.getProperty("os.name").contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}

		try {
			// Loads correct version of this dll
			String JintelliLib;
			if (is64bit) {
				JintelliLib = libDir.getAbsolutePath() + "\\JIntellitype64.dll";
			} else {
				JintelliLib = libDir.getAbsolutePath() + "\\JIntellitype.dll";
			}
			JIntellitype.setLibraryLocation(JintelliLib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Checks if jar was launched from application folder
	private static boolean checkIfGoodDirectory() {
		if (FileUtils.getThisJarFile().equals(mainJar)) {
			return true;
		}
		return false;
	}

}
