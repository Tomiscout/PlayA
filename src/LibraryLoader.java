import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.melloware.jintellitype.JIntellitype;

public class LibraryLoader {

	private static boolean is64bit = false;

	static public void initializeLibraries() {
		if (System.getProperty("os.name").contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}

		try {
			//Loads correct version of this dll
			String exportedLib;
			if (is64bit) {
				exportedLib = ExportLibrary("JIntellitype64.dll");
			} else {
				exportedLib = ExportLibrary("JIntellitype.dll");
			}
			JIntellitype.setLibraryLocation(exportedLib);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Adds lib/ to resource path
	static public String ExportLibrary(String resourceName) throws Exception {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		File exportFolder;
		try {
			stream = LibraryLoader.class.getResourceAsStream("/lib/"+resourceName);
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			exportFolder = new File(FileUtils.getWorkDirectory() + "lib\\");
			if(!exportFolder.exists()) exportFolder.mkdir();

			resStreamOut = new FileOutputStream(exportFolder.getAbsolutePath()+"\\"+resourceName);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			System.out.println("Exception in exportLibrary function");
			ex.printStackTrace();
			throw ex;
		} finally {
			stream.close();
			resStreamOut.close();
		}

		return exportFolder.getAbsolutePath()+"\\"+resourceName;
	}
}
