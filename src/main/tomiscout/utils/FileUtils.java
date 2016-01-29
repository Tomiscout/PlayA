package main.tomiscout.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.DirectFileReadDataSource;
import com.mpatric.mp3agic.Mp3File;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import main.tomiscout.playlistControl.PlaylistWriter;

public class FileUtils {

	public final static Object obj = new Object();
	static String[] forbiddenSymbols = { "/", "\\", "?", "*", ":", "|", "\"", "<", ">" };
	static String[] forbiddenNames = { "CON", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
			"COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", };
	public static String[] supportedAudioFormats = { ".mp3", ".m4a", ".waw", ".aiff" };
	static ImageIcon[] rootIcons = new ImageIcon[2];
	static final String RESOURCESFOLDER = "/main/tomiscout/resources/";
	static FileSystemView fsv = FileSystemView.getFileSystemView();
	private static String workingDir = System.getenv("APPDATA") + "\\Tomiscout\\PlayA\\";
	
	public static String getWorkDirectory() {
		return workingDir;
	}

	public static String truncateFileType(String p) {
		int indexOfPeriod = p.lastIndexOf(".");

		if (indexOfPeriod < 0)
			return p;
		else {
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

	public static String NormalizeName(String absolutePath) {
		String name = absolutePath;
		for (String symbol : forbiddenSymbols) {
			if (!symbol.equals("\\"))
				name = name.replace(symbol, "");
		}
		return name;
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
		return getExcludedFiles(dir, new String[] { ext });
	}

	public static File[] getExcludedFiles(File dir, String[] ext) {
		ArrayList<File> files = new ArrayList<File>();
		if (dir.isDirectory()) {
			File[] rawFiles = dir.listFiles();
			for (File f : rawFiles) {
				String extension = getFileExtension(f.getAbsolutePath());
				if (extension != null) {
					for (String e : ext) {
						if (extension.equals(e))
							files.add(f);
					}
				}
			}
			File[] returnFiles = files.toArray(new File[files.size()]);
			return returnFiles;
		} else {
			return null;
		}
	}

	public static File[] filterFilesByExtention(ArrayList<File> files, String[] ext) {
		ArrayList<File> filtered = new ArrayList<File>();
		for (File f : files) {
			for (String e : ext) {
				if (f.getAbsolutePath().endsWith(e))
					filtered.add(f);
			}
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
		if (file.getAbsolutePath().endsWith(".m4a")) {
			IsoFile isoFile = null;
			try {
				isoFile = new IsoFile(new DirectFileReadDataSource(file));
				double lengthInSeconds = 0;
				if(isoFile.getMovieBox() != null && isoFile.getMovieBox().getMovieHeaderBox() != null){
					lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration()
							/ isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
				}
				return (long) lengthInSeconds;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (isoFile != null)
					try {
						isoFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		} else
			try {
				String filePath = null;
				try {
					// Encoding filename to UTF-8, doesn't support folders with
					// UTF-8 characters
					filePath = file.getParentFile().toURI().toString()
							+ URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				Media media = new Media(filePath);
				MediaPlayer mediaPlayer = new MediaPlayer(media);

				// Sleeps thread because Media is asynchronous
				// takes ~2 loops to get ready status
				int count = 0;
				while (mediaPlayer.getStatus() != Status.READY && count < 20) {
					Thread.sleep(10);
					count++;
				}

				length = (long) media.getDuration().toSeconds();
				
				//If failed to get length use mp3agic library
				if(length <= 0){
					Mp3File mp3file = new Mp3File(file);
					length = mp3file.getLengthInSeconds();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		return length;
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
			if (p.isDirectory()) {
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

	public static String readFile(File f) {
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void copyFile(File from, File to) {
		try {
			Files.copy(from.toPath(), to.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Image getAssetsImage(String name) {
		System.out.println("Loading assets image: " + RESOURCESFOLDER + name);
		InputStream stream = FileUtils.class.getResourceAsStream(RESOURCESFOLDER + name);
		if (stream == null) {
			System.out.println("asset image stream is null!");
			return null;
		}
		Image image = new Image(stream);
		return image;
	}

	public static File getThisJarFile() {
		try {
			return new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public String ExportResource(String resourceName, File folder) throws Exception {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		String outputFile;
		try {
			stream = FileUtils.class.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];

			outputFile = folder.getAbsolutePath() + resourceName.replace("/", "\\");
			System.out.println("Outputing resource:" + outputFile);
			resStreamOut = new FileOutputStream(outputFile);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (stream != null)
				stream.close();
			if (resStreamOut != null)
				resStreamOut.close();
		}

		return outputFile;
	}

}