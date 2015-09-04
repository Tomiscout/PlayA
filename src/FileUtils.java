import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import javafx.scene.image.Image;

public class FileUtils {

	public final static Object obj = new Object();
	static String[] forbiddenSymbols = { "/", "\\", "?", "%", "*", ":", "|", "\"", "<", ">", "." };
	static String[] forbiddenNames = { "CON", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
			"COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", };
	static ImageIcon[] rootIcons = new ImageIcon[2];
	static final String ASSETSFOLDER = "assets/";
	static FileSystemView fsv = FileSystemView.getFileSystemView();
	private static String workingDir = System.getenv("APPDATA") + "\\Tomiscout\\PlayA\\";

	public static String getWorkDirectory() {
		return workingDir;
	}
	public static String truncateFileType(String p){
		int indexOfPeriod = p.lastIndexOf(".");
		String truncated;
		
		if(indexOfPeriod < 0) return p;
		else{
			return p.substring(0, indexOfPeriod);
		}
		
	}

	public static boolean isNameCorrect(String str) {
		if (str == null)
			return false;

		for (String s : forbiddenSymbols) {
			if (str.contains(s)) {
				return false;
			}
		}
		for (String s : forbiddenNames) {
			if (str.contains(s)) {
				return false;
			}

		}
		return true;
	}

	public static String getFileExtension(String path) {
		int lastIndexOf = path.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return null; // empty extension
		}
		return path.substring(lastIndexOf);
	}

	public static String getFileInfo(String path) {
		File f = new File(path);
		if (f.exists()) {
			if (!f.isDirectory()) {
				return path + "�" + f.length();
			} else {
				System.out.println("F is directory");
				return path;
			}
		}
		System.out.println("F doesnt exist");
		return null;
	}

	// Excludes files in a directory by file extention
	public static File[] getExcludedFiles(File dir, String ext) {
		ArrayList<File> files = new ArrayList<File>();
		if (dir.isDirectory()) {
			File[] rawFiles = dir.listFiles();
			for (File f : rawFiles) {
				String extension = getFileExtension(f.getAbsolutePath());
				if (extension != null && extension.equals(ext)) {
					files.add(f);
				}
			}
			File[] returnFiles = files.toArray(new File[files.size()]);
			return returnFiles;
		} else {
			return null;
		}
	}
	
	public static File[] filterFilesByExtention(ArrayList<File> files, String ext){
		ArrayList<File> filtered = new ArrayList<File>();
		for(File f : files){
			if(f.getAbsolutePath().endsWith(ext)) filtered.add(f);
		}
		return filtered.toArray(new File[filtered.size()]);
	}

	// TODO fix header and folder header issues
	// Credit to 'martinus' @Stackoverflow
	public static int countLines(File f) throws IOException {
		if (f.exists())
			try {
				// Reads in UTF-8
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				int count = 0;
				String song;
				while ((song = br.readLine()) != null) {
					if (!song.startsWith(PlaylistWriter.DIRECTORYHEADER)) {
						count++;
					}
				}

				br.close();
				return count - 1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		return -1;
	}

	public static long getSongLength(File file) {
		long length = -1;

		try {
			AudioFile f = AudioFileIO.read(file);
			AudioHeader a = f.getAudioHeader();
			length = a.getTrackLength();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		catch (CannotReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadOnlyFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return length;
	}

	public static String formatSeconds(int s) {
		String string = "";

		if (s < 1)
			return "00:00";

		int remainder;
		int days = (int) s / 86400;
		int hours = (int) (s % 86400) / 3600;
		remainder = s - days * 86400 - hours * 3600;
		int mins = (int) remainder / 60;
		int secs = remainder % 60;

		if (days > 0)
			string += days + "d";
		if (hours > 0)
			string += hours + "h ";

		if (mins < 10)
			string += "0" + mins + ":";
		else
			string += mins + ":";

		if (secs < 10)
			string += "0" + secs;
		else
			string += secs;

		return string;
	}

	public static File[] getSubFolders(File path) {
		Path p = path.toPath();
		ArrayList<File> paths = new ArrayList<File>();
		try {
			Files.walk(p).filter(Files::isDirectory).forEach(e -> paths.add(e.toFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		File[] fileArray = paths.toArray(new File[paths.size()]);
		return fileArray;
	}

	public static File[] getSubFolders(File[] dir) {
		ArrayList<File> subFolders = new ArrayList<File>();

		for (File p : dir) {
			if(p.isDirectory()){
				File[] childs = getSubFolders(p);
				for (File c : childs) {
					subFolders.add(c);
				}
			}
		}
		File[] subFoldersArray = subFolders.toArray(new File[subFolders.size()]);
		return subFoldersArray;
	}

	public static String getFirstLine(File f) {
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			line = br.readLine();
			br.close();
		} catch (IOException ioe) {
			return "";
		}
		return line;
	}
	
	public static void copyFile( File from, File to ){
	    try {
			Files.copy( from.toPath(), to.toPath() );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Image getAssetsImage(String name){
		System.out.println("Loading assets image: "+ASSETSFOLDER+name);
		InputStream stream = FileUtils.class.getResourceAsStream(ASSETSFOLDER+name);
		if(stream == null) {
			System.out.println("asset image stream is null!");
			return null;
		}
		Image image = new Image(stream);
		return image;
	}
}