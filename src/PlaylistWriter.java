import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.TreePath;


public class PlaylistWriter {

	protected static PrintWriter printWriter;
	static int progress;
	public static final String SEPARATOR = "�";
	public static final String DIRECTORYHEADER = "dir:";
	public static final String LENGTHHEADER = "length:";
	public static final String TYPEHEADER = "type:";
	public static final String URLHEADER = "url:";
	public static final String SONGCOUNTHEADER = "count:";

	//TODO Make any File[] input viable
	public static boolean createPlaylist(String name, File[] files) {
		System.out.println("Creating playlist with input length:"+files.length);
		printWriter = null;

		File file = new File(getWorkingDir().getAbsolutePath() + "\\" + name + ".plp");
		file.getParentFile().mkdirs();

		ArrayList<File> rootFiles = new ArrayList<File>();
		ArrayList<File> paths = new ArrayList<File>();
		for(File path : files){
			if(path.isDirectory()){
				File[] treePaths = FileUtils.getSubFolders(path);
				for (File p : treePaths) {
					paths.add(p);
				}
			}else if(path.isFile()){
				rootFiles.add(path);
			}
		}

		File[] rootSongs = FileUtils.filterFilesByExtention(rootFiles, ".mp3");

		// Enables progressbar
		PlaylistPane.enableProgressBar(paths.size());
		progress = 0;

		new Thread() {
			public void run() {
				int songLengths = 0;
				int songCount = 0;
				try {
					printWriter = new PrintWriter(file, "UTF-8");
				} catch (FileNotFoundException e) {
					System.out.println("File not found while creating playist");
				} catch (UnsupportedEncodingException e) {
					System.out.println("Unsupported Encoding in PlaylistWriter.createPlaylist");
				}
				
				int rootIndicator = 0;
				if(rootSongs.length>0) rootIndicator = 1;
				for (int i = 0; i < paths.size()+rootIndicator; i++) {
					File path;
					File[] songs;
					if(1==paths.size()+rootIndicator-1){
						path = rootSongs[0].getParentFile();
						songs = rootSongs;
					}else{
						path = paths.get(i);
						songs = FileUtils.getExcludedFiles(path, ".mp3");
					}
					
					
					if (songs.length != 0) {
						PlaylistPane.setProgressBarItemMax(songs.length);
					}

					printWriter.println(SEPARATOR+DIRECTORYHEADER + path.getAbsolutePath());
					for (int o = 0; o < songs.length; o++) {

						long length = FileUtils.getSongLength(songs[o]);

						songLengths += length;
						songCount++;
						printWriter.println(songs[o].getName() + " " + length);
						PlaylistPane.setProgressBarItemValue(o);
					}
					progress++;
					PlaylistPane.setProgressBarValue(progress);
				}
				PlaylistPane.disableProgressBar();
				printWriter.close();

				String fileName = file.getName();
				fileName = fileName.substring(0, fileName.lastIndexOf(".plp"));
				PlaylistHeader ph = new PlaylistHeader(fileName, 0, songLengths, songCount);
				appendFirstLine(file, ph.toString());
				
				PlaylistPane.folderTable.data.add(ph.getPlaylistObject());
			}
		}.start();

		return true;
	}

	// Playlist merger
	public static void createCustomPlaylist(String name, String[] playlists) {
		Set<String> foldersUsed = new TreeSet<String>(new StringComparator());
		ArrayList<String> content = new ArrayList<String>();
		ArrayList<String> playlistLenghts = new ArrayList<String>();
		List<String> duplicates = new ArrayList<String>();

		for (String playlistName : playlists) {
			File pFile = new File(getWorkingDir().getAbsolutePath() + "\\" + playlistName + ".plp");

			if (pFile.exists()) {
				try {
					// Reads in UTF-8
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pFile), "UTF-8"));

					String line;
					if ((line = br.readLine()) != null) {
						playlistLenghts.add(line);
					}
					while ((line = br.readLine()) != null) {
						if (line.startsWith(SEPARATOR+DIRECTORYHEADER)) {
							// Checks for duplicates
							if (!foldersUsed.add(line)) {
								duplicates.add(line);
								System.out.println("Duplicated folder:" + line);
							}
						}
						content.add(line);
					}

					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Treats duplicates
		if (!duplicates.isEmpty()) {
			for (String s : duplicates) {
				Set<String> filteredSongs = new TreeSet<String>(new StringComparator());
				boolean isSaving = false;

				// Gets all songs from duped folder
				for (int i = 0; i < content.size(); i++) {
					if (content.get(i).equals(s) && isSaving == false) {
						isSaving = true;
						continue;
					}
					if (content.get(i).startsWith(SEPARATOR+DIRECTORYHEADER) && isSaving == true) {
						if (!content.get(i).equals(s)) {
							isSaving = false;// If same folder header occurs
												// again ignore
							continue;
						} else
							continue;
					}
					if (isSaving) {
						System.out.println("Adding song: " + content.get(i));

						// adds songs to the array, unless duplicate
						filteredSongs.add(content.get(i));

					}
				}
				boolean isDeleted = false;
				// Removing duplicates folders from content array
				for (int i = 0; i < content.size(); i++) {
					if (isDeleted) {
						i--;
						isDeleted = false;
					}
					if (content.get(i).equals(s)) {
						// Remove lines until other playlistheader occured
						content.remove(i);
						while (!content.get(i).startsWith(SEPARATOR+DIRECTORYHEADER)) {
							content.remove(i);
						}
						isDeleted = true;
					}
				}
				// adds filtered folder
				content.add(s);
				content.addAll(filteredSongs);
			}
			PlaylistPane.reloadPlaylists();
		}

		// Gets playlist lenghts
		long length = 0;
		for (String s : playlistLenghts) {
			try {
				length += Long.parseLong(s);
			} catch (NumberFormatException e) {
				System.out.println("Couldnt convert to long:" + s);
			}
		}

		// prints contents to file
		try {
			printWriter = new PrintWriter(new File(getWorkingDir().getAbsolutePath() + "\\" + name + ".plp"), "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("File not found while creating playist");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding in PlaylistWriter.createPlaylist");
		}

		// Writes file header
		printWriter.println(length);

		// writes content
		for (String s : content) {
			printWriter.println(s);
		}
		printWriter.close();
	}

	// String comparator
	public static class StringComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareTo(s2);
		}
	}

	// TODO Check if song already exists
	public static boolean addSongToFile(String name, String dir) {
		File tFile = new File(getWorkingDir().getAbsolutePath() + "\\" + name + ".plp");
		if (tFile.exists()) {
			FileWriter fw;
			try {
				fw = new FileWriter(tFile);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(dir + "\n");
				bw.flush();
				bw.close();
				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return true;
	}

	// FIX header issues not loading first song
	// First line is playlist info header
	public static String[] readPlaylist(String name) {
		File tFile = new File(getWorkingDir().getAbsolutePath() + "\\" + name + ".plp");
		String song;
		ArrayList<String> songs = new ArrayList<String>();

		if (tFile.exists()) {

			try {
				// Reads in UTF-8
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tFile), "UTF-8"));

				String currentDir = "";

				while ((song = br.readLine()) != null) {
					if (song.startsWith(SEPARATOR+DIRECTORYHEADER)) {
						currentDir = song.substring(5);
						continue;
					}
					if (!currentDir.isEmpty()) {
						songs.add(currentDir + "\\" + song);
					}

				}

				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String[] rSongs = songs.toArray(new String[songs.size()]);
		return rSongs;
	}

	public static int getCurrentProgress() {
		return progress;
	}

	public static void appendFirstLine(File f, String line) {
		ArrayList<String> strings = new ArrayList<String>();
		String string;
		try {
			// Reads in UTF-8
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

			while ((string = br.readLine()) != null) {
				strings.add(string);
			}
			br.close();

			PrintWriter pw = new PrintWriter(f, "UTF-8");
			pw.println(line);
			for (String s : strings) {
				pw.println(s);
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static int getPlaylistLength(File playlist){
		
		String line = FileUtils.getFirstLine(playlist);
		if(line == null) return 0;
	
		//TODO when expanding header extract the number only
		//String number = line.substring(0, line.indexOf(" "));
		
		int n;
		try{
			n = Integer.parseInt(line);
		}catch(NumberFormatException e){
			//Weird bug when line starts with "?", occurs when created files manually
			try{
				n = Integer.parseInt(line.substring(1));
			}catch(NumberFormatException e2){
				n = 0;
			}			
		}
		return n;
	}
	
	public static File getWorkingDir(){
		File workingDir = new File(FileUtils.getWorkDirectory()+"playlists\\");
		if(!workingDir.exists()) workingDir.mkdirs();
		return workingDir;
	}
	
}
