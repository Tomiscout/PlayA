import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.googlecode.mp4parser.DirectFileReadDataSource;
import com.googlecode.mp4parser.boxes.apple.AppleCoverBox;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class FileUtils {

	public final static Object obj = new Object();
	static String[] forbiddenSymbols = { "/", "\\", "?", "*", ":", "|", "\"", "<", ">" };
	static String[] forbiddenNames = { "CON", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
			"COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", };
	static String[] supportedAudioFormats = { ".mp3", ".m4a", ".waw", ".ogg" };
	static ImageIcon[] rootIcons = new ImageIcon[2];
	static final String ASSETSFOLDER = "assets/";
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
		try {
			String filePath = null;
			try {
				//Encoding filename to UTF-8, doesn't support folders with UTF-8 characters
				filePath = file.getParentFile().toURI().toString()
						+ URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			Media media = new Media(filePath);
			MediaPlayer mediaPlayer = new MediaPlayer(media);

			// Sleeps thread because Media is asynchronous, count to avoid infinite loop
			// takes ~2 loops to get ready status
			int count = 0;
			while (mediaPlayer.getStatus() != Status.READY && count<20) {
				Thread.sleep(10);
				count++;
			}
			
			length = (long) media.getDuration().toSeconds();
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
	
	public static String readFile(File f){
		try(BufferedReader br = new BufferedReader(new FileReader(f))) {
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
		System.out.println("Loading assets image: " + ASSETSFOLDER + name);
		InputStream stream = FileUtils.class.getResourceAsStream(ASSETSFOLDER + name);
		if (stream == null) {
			System.out.println("asset image stream is null!");
			return null;
		}
		Image image = new Image(stream);
		return image;
	}
	
	public static File getThisJarFile(){
		try {
			return new File(FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Walks down mp4 structure to get cover art image
	public static BufferedImage getCoverArtFromMp4File(File f){
		IsoFile isoFile = null;
		try {
			isoFile = new IsoFile(new DirectFileReadDataSource(f));
		} catch (IOException e) {
			e.printStackTrace();
		}

		
        MovieBox moov = (MovieBox) isoFile.getMovieBox();
        UserDataBox uData = null;
        if(moov != null) {
        	List<Box> boxes = moov.getBoxes();
        	//Finds UserDataBox
        	for(Box b : boxes){
        		if(b instanceof UserDataBox)uData = (UserDataBox)b;
        	}
        }
		AppleCoverBox cover = null; //CoverArt box
        if(uData != null){
        	MetaBox metaBox = (MetaBox) uData.getBoxes().get(0);
        	if(metaBox != null){
        		AppleItemListBox ib = null;
        		List<Box> appleBoxes = metaBox.getBoxes();
        		
        		//Finds AppleItemListBox from metadata boxes
        		for(Box appleBox : appleBoxes){
        			if(appleBox instanceof AppleItemListBox){
        				ib = (AppleItemListBox) appleBox;
        				break;
        			}
        		}
        		
        		if(ib != null){
        			List<Box> itemListBox = ib.getBoxes();
        			//Finds AppleCoverBox from AppleItemListBox boxes
	            	for(Box b : itemListBox){
	            		if(b instanceof AppleCoverBox) {
	            			cover = (AppleCoverBox)b;
	            			break;
	            		}
	            	}
        		}else{
        			System.out.println("No AppleItemListBox");
        		}
        	}
        }
        
        //If file contains coverArt
        if(cover != null){
        	byte[] imageData = cover.getCoverData();
        	InputStream in = new ByteArrayInputStream(imageData);
			try {
				BufferedImage bi = ImageIO.read(in);
				return bi;
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return null;
	}

	public static BufferedImage getCoverArtFromMp3File(File f){
		MP3File mp3;
		java.awt.Image cover = null;
		try {
			mp3 = (MP3File) AudioFileIO.read(f);
			AbstractID3v2Tag v24tag = mp3.getID3v2TagAsv24();

			Artwork artWork = null;
			if (v24tag != null)
				artWork = v24tag.getFirstArtwork();

			if (artWork != null) {
				cover = (java.awt.Image) artWork.getImage();
			} else {
				System.out.println("No cover art");
			}
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		
		if(cover != null){
			return DataUtils.toBufferedImage(cover);
		}
		
		return null;
	}
}