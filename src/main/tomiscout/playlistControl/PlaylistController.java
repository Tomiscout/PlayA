package main.tomiscout.playlistControl;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import main.tomiscout.ui.FXMediaPlayer;
import main.tomiscout.ui.MainGui;
import main.tomiscout.ui.playlist.PlaylistPane;

public class PlaylistController {
	static ArrayList<File> currentSongs = new ArrayList<File>();
	static ArrayList<File> previousSongs = new ArrayList<File>();
	public static File currentSong;
	private static boolean isShuffle = true;
	private static boolean isRepeat = false;
	static File workingDir = PlaylistWriter.getWorkingDir();

	public static void openPlaylist(String name, boolean clearList) {
		String[] songs = PlaylistWriter.readPlaylist(name);

		if (clearList) {
			currentSongs.clear();
			MainGui.table.getData().clear();
		}

		for (String song : songs) {
			PlaylistWriter.SongObject songObj = PlaylistWriter.parseSongObject(song);
			MainGui.table.getData().add(songObj);
			currentSongs.add(songObj.getFile());
		}

	}

	public static void openPlaylist(String[] names, boolean clearList) {
		if (clearList) {
			currentSongs.clear();
			MainGui.table.getData().clear();
		}
		for (String name : names) {
			openPlaylist(name, false);
		}
	}

	// Called when song ends.
	public static void playNextSong() {
		if (currentSongs.isEmpty())
			return;

		if (currentSong == null)
			return;

		// Gives priority to repeating song
		if (isRepeat) {
			playSong(currentSong, true);
		} else if (isShuffle) {
			Random rand = new Random();
			int randomNum;
			boolean isSame = false;
			if (currentSongs.size() > 5) {
				if (!previousSongs.isEmpty())
					do {
						randomNum = rand.nextInt(currentSongs.size());

						int previousLoop = 0;
						if (previousSongs.size() > 5)
							previousLoop = previousSongs.size();
						for (int i = previousSongs.size() - 1; i >= previousLoop; i--) {
							if (previousSongs.get(i).equals(currentSongs.get(randomNum))) {
								isSame = true;
								break;
							}
						}
						System.out.println(isSame);
					} while (isSame);
				else {
					randomNum = rand.nextInt(currentSongs.size());
				}
			} else {
				randomNum = rand.nextInt(currentSongs.size());
			}
			File song = currentSongs.get(randomNum);
			previousSongs.add(song);
			playSong(song, true);
		} else {
			for (int i = 0; i < currentSongs.size(); i++) {
				if (currentSongs.get(i).equals(currentSong)) {
					if (i == currentSongs.size() - 1) {
						previousSongs.add(currentSong);
						playSong(currentSongs.get(0), true);
						return;
					} else {
						previousSongs.add(currentSong);

						playSong(currentSongs.get(i + 1), true);
						return;
					}
				}
			}
			playSong(currentSongs.get(0), true);
		}
	}

	public static void playPreviousSong() {
		if (previousSongs.isEmpty())
			return;

		int prevSong = previousSongs.size() - 1;
		currentSong = previousSongs.get(prevSong);

		if (currentSong != null) {
			previousSongs.remove(prevSong);
			FXMediaPlayer.play(currentSong, true);
		}
	}

	public static void playSong(File file, boolean force) {
		FXMediaPlayer.play(file, force);
		if (currentSong != null)
			previousSongs.add(currentSong);
		currentSong = file;
	}

	public static boolean toggleShuffle() {
		isShuffle = !isShuffle;
		return isShuffle;
	}

	public static boolean toggleRepeat() {
		isRepeat = !isRepeat;
		return isRepeat;
	}

	public static void DeleteSelectedPlaylists() {
		PlaylistPane.DeleteSelectedPlaylists();
	}

	public static void RenamePlaylist() {
		PlaylistPane.DeleteSelectedPlaylists();
	}

	public static void OpenSelectedPlaylists() {
		PlaylistPane.OpenSelectedPlaylists();
	}

	public static void RescanSelectedPlaylists() {
		PlaylistPane.RescanSelectedPlaylists();

	}
}
