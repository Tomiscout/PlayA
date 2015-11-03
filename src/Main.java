import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.melloware.jintellitype.JIntellitype;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.codebrig.honcho.api.InvalidAPISettingsException;
import com.codebrig.honcho.api.SeverConnectionException;
import com.codebrig.honcho.api.update.AlphaOmega;
import com.codebrig.honcho.api.update.Update;
import com.codebrig.honcho.api.update.UpdateAPI;

public class Main extends Application {
	static String appdata = System.getenv("APPDATA");
	static File libDir = new File(appdata + "\\Tomiscout\\PlayA\\lib\\");
	static File mainJar = new File(appdata + "\\Tomiscout\\PlayA\\PlayA.jar");

	private final static String API_USER = "USER-IHBC6wRy9QgS3xN1jbB8854W6uL9ltJI0vZSe2I2Oaz8O21m5J4BaNt0Z91Y";
	private final static String API_KEY = "KEY-0O05rnKQ5RFM43vINjAxG34kEWmsNJRQ3Jt2LQVmG1s3KJbLHbmw85x3JQS0s";
	private final static String PROJECT_IDENTIFIER = "HON-WwEV4PC0LEbmo3G4NW3wR3edU0o7yPi264cEEldo7gMndXo8G6WZ7538IL5M1";
	private final static String CURRENT_VERSION = "0.7";
	private final static File API_LOCATION = new File(libDir.getAbsolutePath() + "\\honcho-api.jar");

	private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

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

			primaryStage.setTitle("PlayA "+CURRENT_VERSION);
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

		// Checks for updates after 1 seconds
		checkForUpdates(1);
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

	public static void checkForUpdates(int seconds) {
		Runnable task = new Runnable() {
			public void run() {
				try {
					final UpdateAPI api = new UpdateAPI(new BasicAlphaOmega(), API_USER, API_KEY, PROJECT_IDENTIFIER,
							CURRENT_VERSION, API_LOCATION);

					// uncomment the line below if you have trouble checking for
					// updates
					 //api.setDebugLogEnabled (true);

					 System.out.println("Checking for updates");
					final Update update = api.checkForUpdates();
					if (update.isNewVersion()) {
						api.performUpdateNow(update);
					} else {
						// no update available. seed our current files so others
						// can update
						api.setSeedingMode(true);
					}
				} catch (InvalidAPISettingsException ex) {
					// check your API settings as something is wrong
					ex.printStackTrace();
				} catch (SeverConnectionException ex) {
					// Typically means a user does not have an active internet
					// connection.
					ex.printStackTrace();
				}
			}
		};
		worker.schedule(task, seconds, TimeUnit.SECONDS);

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

	/**
	 * Basic AlphaOmega class
	 */
	static class BasicAlphaOmega extends AlphaOmega {
		@Override
		public void terminate() {
			// clean up/close any open threads, IO, etc.
			// no need to call System.exit() as it will be done following this
			// method
			Platform.exit();
		}

		@Override
		public String[] executeCommand() {
			// command = java -jar TestApplication.jar
			// this command is used to re-run the main application after an
			// update
			System.out.println("Relaunching app");
			return new String[] { "java -jar PlayA.jar" };
		}

	}

}

// TODO

// downloading from yt
// drag'n'drop
// song ratios
