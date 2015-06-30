import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class FileUtils {

	public final static Object obj= new Object();
	static String[] forbiddenSymbols = { "/", "\\", "?", "%", "*", ":", "|",
			"\"", "<", ">", "." };
	static String[] forbiddenNames = { "CON", "PRN", "AUX", "CLOCK$", "NUL",
			"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
			"COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7",
			"LPT8", "LPT9", };
	static ImageIcon[] rootIcons = new ImageIcon[2];
	static FileSystemView fsv = FileSystemView.getFileSystemView();
	private static String workingDir = System.getenv("APPDATA")
			+ "\\Tomiscout\\PlayA\\";

	public static String getWorkDirectory() {
		return workingDir;
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
				return path + "¥" + f.length();
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

	// Credit to 'martinus' @Stackoverflow
	public static int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

	public static long getSongLength(File f) {
		long length = -1;
		try {
			Mp3File mp3file = new Mp3File(f.getAbsolutePath());
			length = mp3file.getLengthInSeconds();
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return length;
	}

	public static String formatSeconds(int s) {
		String string = "";
		
		if(s<1) return "00:00";
		
		int remainder;
		int days = (int) s / 86400;
		int hours = (int) (s % 86400)/3600;
		remainder = s - days*86400 - hours*3600;
		int mins =(int) remainder / 60;
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

	public static Path[] getSubFolders(Path path) {
		ArrayList<Path> paths = new ArrayList<Path>();
		try {
			Files.walk(path).filter(Files::isDirectory)
					.forEach(e -> paths.add(e));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path[] pathArray = paths.toArray(new Path[paths.size()]);
		return pathArray;
	}

	public static Path[] getSubFolders(Path[] dir) {
		ArrayList<Path> subFolders = new ArrayList<Path>();

		for (Path p : dir) {
			Path[] childs = getSubFolders(p);
			for (Path c : childs) {
				subFolders.add(c);
			}
		}
		Path[] subFoldersArray = subFolders
				.toArray(new Path[subFolders.size()]);
		return subFoldersArray;
	}

	public static String getFirstLine(File f) {
		String line;
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));
			
			line = br.readLine();
			br.close();
		}catch(IOException ioe){
			return "";
		}
		return line;
	}
}