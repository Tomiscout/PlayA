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
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

public class PlaylistWriter {

	protected static File workingDir = new File(FileUtils.getWorkDirectory());
	protected static PrintWriter printWriter;
	static int progress;

	public static boolean createPlaylist(String name,
			ArrayList<TreePath> folders, boolean subFolders) {
		if (!workingDir.exists())
			workingDir.mkdirs();// Creates directories

		printWriter = null;

		// TODO If it already exists
		// .plp
		File file = new File(workingDir.getAbsolutePath() + "\\" + name
				+ ".plp");
		file.getParentFile().mkdirs();

		ArrayList<Path> paths = new ArrayList<Path>();
		for (int i = 0; i < folders.size(); i++) {
			Path currentPath = Paths.get((folders.get(i).getLastPathComponent()
					.toString()));

			// If requires subfolders
			if (subFolders) {
				Path[] treePaths = FileUtils.getSubFolders(currentPath);
				for (Path p : treePaths) {
					paths.add(p);
				}
			} else {
				paths.add(currentPath);
			}
		}

		List<String> al = new ArrayList<>();

		for (Path p : paths) {
			al.add(p.toString());
		}

		// Removes duplicate Directories
		if (subFolders) {
			Set<String> hs = new HashSet<>();
			hs.addAll(al);
			al.clear();
			al.addAll(hs);
		}

		// Enables progressbar
		PlaylistPane.enableProgressBar(al.size());
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
					System.out
							.println("Unsupported Encoding in PlaylistWriter.createPlaylist");
				}
				for (String path : al) {
					File[] songs = FileUtils.getExcludedFiles(new File(path),
							".mp3");

					if (songs.length != 0) {
						PlaylistPane.setProgressBarItemMax(songs.length);
					}

					for (int o = 0; o < songs.length; o++) {
						int length = FileUtils.getSongLength(songs[o]);
						songLengths += length;
						songCount++;
						printWriter.println(songs[o].getAbsolutePath() + " "
								+ length);
						PlaylistPane.setProgressBarItemValue(o);
					}
					progress++;
					PlaylistPane.setProgressBarValue(progress);
				}
				PlaylistPane.disableProgressBar();
				printWriter.close();
				appendFirstLine(file, songLengths + "");
				String fileName = file.getName();
				PlaylistPane.data.add(new PlaylistObject(fileName.substring(0,
						fileName.lastIndexOf(".plp")), songCount, FileUtils
						.formatSeconds(songLengths)));
			}
		}.start();

		return true;
	}

	// TODO Check if song already exists
	public static boolean addSongToFile(String name, String dir) {
		File tFile = new File(workingDir.getAbsolutePath() + "\\" + name
				+ ".plp");
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

	public static String[] readPlaylist(String name) {
		File tFile = new File(workingDir.getAbsolutePath() + "\\" + name
				+ ".plp");
		String song;
		ArrayList<String> songs = new ArrayList<String>();

		if (tFile.exists()) {

			try {
				// Reads in UTF-8
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(tFile), "UTF-8"));

				br.readLine();//Skips .plp header
				while ((song = br.readLine()) != null) {
					songs.add(song);
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
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));

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
}
