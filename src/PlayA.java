import java.io.File;
import java.net.URISyntaxException;

import com.melloware.jintellitype.JIntellitype;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PlayA extends Application {
	static File appdata = new File(System.getenv("APPDATA"));
	static File libDir = new File(appdata.getAbsolutePath() + "\\Tomiscout\\PlayA\\lib\\");
	static final String VERSION = "0.8";
	
	static Stage pStage;
	static Scene scene;
	Button button;

	static boolean is64bit = false;
	static boolean isEclipse = true;

	static final double MINWIDTH = 800;
	static final double MINHEIGHT = 560;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			pStage = primaryStage;
			MainGui gui = new MainGui();

			primaryStage.setTitle("PlayA " + VERSION);
			primaryStage.getIcons().add(FileUtils.getAssetsImage("Icon.png"));
			scene = new Scene(gui, MINWIDTH, MINHEIGHT);

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
		System.exit(1);
	}

	public static Stage getPrimaryStage() {
		return pStage;
	}

	private void initializeLibraries() {
		if (!libDir.exists())
			libDir.mkdirs();

		if (System.getProperty("os.name").contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}

		File libFile = null;
		boolean loadLocaly = true;
		//Checks if program is ran from eclipse or .jar
		try {
			File jarFile = new File(PlayA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if(jarFile.getAbsolutePath().endsWith(".jar")) isEclipse = false; //Checks if this is running from eclipse
			
			//Checks if there's local lib folder, if so, load from it
			libFile = new File(jarFile.getAbsolutePath()+"\\lib\\JIntellitype64.dll");
			if(!isEclipse && !libFile.exists()){
				libFile = new File(jarFile.getAbsolutePath()+"\\lib\\JIntellitype.dll");
				if(!libFile.exists()) loadLocaly = false;
			}else{
				loadLocaly = false;
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		try {
			// Loads correct version of this dll
			String JintelliLib;
			
			if(loadLocaly){
				if (is64bit) {
					JintelliLib = libFile.getParent() + "\\JIntellitype64.dll";
				} else {
					JintelliLib = libFile.getParent() + "\\JIntellitype.dll";
				}
			}else{
				//Export libraries if not found
				libFile = new File(libDir.getAbsolutePath() + "\\JIntellitype.dll");
				File lib64File = new File(libDir.getAbsolutePath() + "\\JIntellitype64.dll");
				if(!libFile.exists() || !lib64File.exists()) {
					FileUtils.ExportResource("/lib/JIntellitype.dll", libDir.getParentFile());
					FileUtils.ExportResource("/lib/JIntellitype64.dll", libDir.getParentFile());
				}
				
				if (is64bit) {
					JintelliLib = libDir.getAbsolutePath() + "\\JIntellitype64.dll";
				} else {
					JintelliLib = libDir.getAbsolutePath() + "\\JIntellitype.dll";
				}
			}
			System.out.println("Loading JLintelliType:"+JintelliLib);
			
			JIntellitype.setLibraryLocation(JintelliLib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setWidth(double width){
		if(width<MINWIDTH)
			pStage.setHeight(MINWIDTH);
		else
			pStage.setHeight(width);
	}
	public static void setHeight(double height){
		if(height<MINHEIGHT)
			pStage.setHeight(MINHEIGHT);
		else
			pStage.setHeight(height);
	}
}
